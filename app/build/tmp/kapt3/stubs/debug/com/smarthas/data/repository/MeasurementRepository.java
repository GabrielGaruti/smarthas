package com.smarthas.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J@\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\f2\u0006\u0010\u000e\u001a\u00020\n2\u0006\u0010\u000f\u001a\u00020\n2\b\u0010\u0010\u001a\u0004\u0018\u00010\nH\u0086@\u00a2\u0006\u0002\u0010\u0011J\u0016\u0010\u0012\u001a\u00020\b2\u0006\u0010\u0013\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\u0014J\u0012\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00180\u00170\u0016J\u0010\u0010\u0019\u001a\u0004\u0018\u00010\u0018H\u0086@\u00a2\u0006\u0002\u0010\u001aJ\u000e\u0010\u001b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00180\u0016J\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\f0\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001d"}, d2 = {"Lcom/smarthas/data/repository/MeasurementRepository;", "", "api", "Lcom/smarthas/data/api/SmartHasApi;", "dao", "Lcom/smarthas/data/database/MeasurementDao;", "(Lcom/smarthas/data/api/SmartHasApi;Lcom/smarthas/data/database/MeasurementDao;)V", "createMeasurement", "", "token", "", "systolic", "", "diastolic", "date", "time", "notes", "(Ljava/lang/String;IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteMeasurement", "id", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllMeasurements", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/smarthas/data/database/Measurement;", "getLatestMeasurement", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getLatestMeasurementFlow", "getMeasurementCount", "app_debug"})
public final class MeasurementRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.smarthas.data.api.SmartHasApi api = null;
    @org.jetbrains.annotations.NotNull()
    private final com.smarthas.data.database.MeasurementDao dao = null;
    
    public MeasurementRepository(@org.jetbrains.annotations.NotNull()
    com.smarthas.data.api.SmartHasApi api, @org.jetbrains.annotations.NotNull()
    com.smarthas.data.database.MeasurementDao dao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.smarthas.data.database.Measurement>> getAllMeasurements() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.lang.Integer> getMeasurementCount() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object createMeasurement(@org.jetbrains.annotations.NotNull()
    java.lang.String token, int systolic, int diastolic, @org.jetbrains.annotations.NotNull()
    java.lang.String date, @org.jetbrains.annotations.NotNull()
    java.lang.String time, @org.jetbrains.annotations.Nullable()
    java.lang.String notes, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteMeasurement(int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getLatestMeasurement(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.smarthas.data.database.Measurement> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.smarthas.data.database.Measurement> getLatestMeasurementFlow() {
        return null;
    }
}