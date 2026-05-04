package com.smarthas.data.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\bf\u0018\u0000 \u00142\u00020\u0001:\u0001\u0014J\"\u0010\u0002\u001a\u00020\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u001e\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00030\n2\b\b\u0001\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u000bJ\u0018\u0010\f\u001a\u00020\r2\b\b\u0001\u0010\u0006\u001a\u00020\u000eH\u00a7@\u00a2\u0006\u0002\u0010\u000fJ\u0018\u0010\u0010\u001a\u00020\u00112\b\b\u0001\u0010\u0006\u001a\u00020\u0012H\u00a7@\u00a2\u0006\u0002\u0010\u0013\u00a8\u0006\u0015"}, d2 = {"Lcom/smarthas/data/api/SmartHasApi;", "", "createMeasurement", "Lcom/smarthas/data/api/MeasurementResponse;", "token", "", "request", "Lcom/smarthas/data/api/MeasurementRequest;", "(Ljava/lang/String;Lcom/smarthas/data/api/MeasurementRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMeasurements", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "login", "Lcom/smarthas/data/api/LoginResponse;", "Lcom/smarthas/data/api/LoginRequest;", "(Lcom/smarthas/data/api/LoginRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "register", "Lcom/smarthas/data/api/RegisterResponse;", "Lcom/smarthas/data/api/RegisterRequest;", "(Lcom/smarthas/data/api/RegisterRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "app_debug"})
public abstract interface SmartHasApi {
    @org.jetbrains.annotations.NotNull()
    public static final com.smarthas.data.api.SmartHasApi.Companion Companion = null;
    
    @retrofit2.http.POST(value = "auth/register")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object register(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.smarthas.data.api.RegisterRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.smarthas.data.api.RegisterResponse> $completion);
    
    @retrofit2.http.POST(value = "auth/login")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object login(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.smarthas.data.api.LoginRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.smarthas.data.api.LoginResponse> $completion);
    
    @retrofit2.http.GET(value = "measurements")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMeasurements(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.smarthas.data.api.MeasurementResponse>> $completion);
    
    @retrofit2.http.POST(value = "measurements")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createMeasurement(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.smarthas.data.api.MeasurementRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.smarthas.data.api.MeasurementResponse> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0005\u001a\u00020\u00062\u000e\u0010\u0007\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00040\bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/smarthas/data/api/SmartHasApi$Companion;", "", "()V", "BASE_URL", "", "create", "Lcom/smarthas/data/api/SmartHasApi;", "tokenProvider", "Lkotlin/Function0;", "app_debug"})
    public static final class Companion {
        @org.jetbrains.annotations.NotNull()
        private static final java.lang.String BASE_URL = "http://10.0.2.2:8000/";
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.smarthas.data.api.SmartHasApi create(@org.jetbrains.annotations.NotNull()
        kotlin.jvm.functions.Function0<java.lang.String> tokenProvider) {
            return null;
        }
    }
}