import os
from contextlib import asynccontextmanager
from datetime import datetime, timedelta
from typing import Optional, List
from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import jwt, JWTError
from passlib.context import CryptContext
from sqlmodel import SQLModel, Field, Session, create_engine, select
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

# -------------------------
# Config
# -------------------------
JWT_SECRET = os.getenv("SMARTHAS_JWT_SECRET", "secret_key_123")
JWT_ALG = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60 * 24 * 7 # 1 week
DB_URL = "sqlite:///./smarthas.db"

engine = create_engine(
    DB_URL,
    echo=False,
    connect_args={"check_same_thread": False}
)

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/auth/login")

# -------------------------
# Database Models
# -------------------------
class User(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    fullName: str
    email: str = Field(index=True, unique=True)
    password_hash: str
    created_at: datetime = Field(default_factory=datetime.utcnow)

class Measurement(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    user_id: int = Field(index=True)
    systolic: int
    diastolic: int
    date: str
    time: str
    notes: Optional[str] = None
    created_at: datetime = Field(default_factory=datetime.utcnow)

# -------------------------
# API Models
# -------------------------
class UserResponse(BaseModel):
    id: int
    email: str
    fullName: str

class LoginRequest(BaseModel):
    email: str
    password: str

class LoginResponse(BaseModel):
    token: str
    user: UserResponse

class RegisterRequest(BaseModel):
    fullName: str
    email: str
    password: str

class RegisterResponse(BaseModel):
    message: str
    user: UserResponse

class MeasurementRequest(BaseModel):
    systolic: int
    diastolic: int
    date: str
    time: str
    notes: Optional[str] = None

class MeasurementResponse(BaseModel):
    id: int
    systolic: int
    diastolic: int
    date: str
    time: str
    notes: Optional[str]
    createdAt: str

# -------------------------
# Helpers
# -------------------------
def create_db_and_tables() -> None:
    SQLModel.metadata.create_all(engine)

def verify_password(plain: str, hashed: str) -> bool:
    return pwd_context.verify(plain, hashed)

def hash_password(plain: str) -> str:
    return pwd_context.hash(plain)

def create_access_token(sub: str) -> str:
    expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode = {"sub": sub, "exp": expire}
    return jwt.encode(to_encode, JWT_SECRET, algorithm=JWT_ALG)

def get_current_user(token: str = Depends(oauth2_scheme)) -> User:
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Token invalido",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALG])
        email: str | None = payload.get("sub")
        if email is None:
            raise credentials_exception
    except JWTError:
        raise credentials_exception

    with Session(engine) as session:
        user = session.exec(select(User).where(User.email == email)).first()
        if not user:
            raise credentials_exception
        return user

# -------------------------
# App
# -------------------------
@asynccontextmanager
async def lifespan(app: FastAPI):
    create_db_and_tables()
    yield

app = FastAPI(title="SmartHAS API", version="1.0.0", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/health")
def health():
    return {"status": "ok", "service": "smarthas-api"}

@app.post("/auth/register", response_model=RegisterResponse)
def register(payload: RegisterRequest):
    email = payload.email.strip().lower()
    with Session(engine) as session:
        existing = session.exec(select(User).where(User.email == email)).first()
        if existing:
            raise HTTPException(status_code=400, detail="E-mail ja cadastrado")

        user = User(
            fullName=payload.fullName.strip(),
            email=email,
            password_hash=hash_password(payload.password),
        )
        session.add(user)
        try:
            session.commit()
            session.refresh(user)
            return RegisterResponse(
                message="Usuario cadastrado com sucesso",
                user=UserResponse(id=user.id, email=user.email, fullName=user.fullName)
            )
        except Exception as e:
            session.rollback()
            raise HTTPException(status_code=500, detail="Erro ao salvar no banco")

@app.post("/auth/login", response_model=LoginResponse)
def login(payload: LoginRequest): # Changed to ONLY expect JSON as the Android client sends
    email = payload.email.strip().lower()
    password = payload.password

    with Session(engine) as session:
        user = session.exec(select(User).where(User.email == email)).first()
        if not user or not verify_password(password, user.password_hash):
            raise HTTPException(status_code=401, detail="Email ou senha invalidos")

        token = create_access_token(sub=user.email)
        return LoginResponse(
            token=token,
            user=UserResponse(id=user.id, email=user.email, fullName=user.fullName)
        )

@app.post("/measurements", response_model=MeasurementResponse)
def create_measurement(payload: MeasurementRequest, user: User = Depends(get_current_user)):
    m = Measurement(
        user_id=user.id,
        systolic=payload.systolic,
        diastolic=payload.diastolic,
        date=payload.date,
        time=payload.time,
        notes=payload.notes,
    )
    with Session(engine) as session:
        session.add(m)
        session.commit()
        session.refresh(m)
        return MeasurementResponse(
            id=m.id,
            systolic=m.systolic,
            diastolic=m.diastolic,
            date=m.date,
            time=m.time,
            notes=m.notes,
            createdAt=m.created_at.isoformat()
        )

@app.get("/measurements", response_model=List[MeasurementResponse])
def list_measurements(user: User = Depends(get_current_user)):
    with Session(engine) as session:
        q = select(Measurement).where(Measurement.user_id == user.id).order_by(Measurement.id.desc())
        items = session.exec(q).all()
        return [
            MeasurementResponse(
                id=i.id,
                systolic=i.systolic,
                diastolic=i.diastolic,
                date=i.date,
                time=i.time,
                notes=i.notes,
                createdAt=i.created_at.isoformat()
            ) for i in items
        ]
