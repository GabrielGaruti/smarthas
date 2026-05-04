package com.smarthas.data.database;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\t\n\u0002\b\u0004\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\bH\'J\u0010\u0010\u000b\u001a\u0004\u0018\u00010\nH\u00a7@\u00a2\u0006\u0002\u0010\fJ\u0010\u0010\r\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\n0\bH\'J\u0018\u0010\u000e\u001a\u0004\u0018\u00010\n2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00050\bH\'J\u0016\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u0013J\u0016\u0010\u0014\u001a\u00020\u00032\u0006\u0010\u0012\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u0013\u00a8\u0006\u0015"}, d2 = {"Lcom/smarthas/data/database/MeasurementDao;", "", "delete", "", "id", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllMeasurements", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/smarthas/data/database/Measurement;", "getLatestMeasurement", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getLatestMeasurementFlow", "getMeasurementById", "getMeasurementCount", "insert", "", "measurement", "(Lcom/smarthas/data/database/Measurement;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "update", "app_debug"})
@androidx.room.Dao()
public abstract interface MeasurementDao {
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.smarthas.data.database.Measurement measurement, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object update(@org.jetbrains.annotations.NotNull()
    com.smarthas.data.database.Measurement measurement, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM measurements ORDER BY id DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.smarthas.data.database.Measurement>> getAllMeasurements();
    
    @androidx.room.Query(value = "SELECT * FROM measurements WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMeasurementById(int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.smarthas.data.database.Measurement> $completion);
    
    @androidx.room.Query(value = "DELETE FROM measurements WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object delete(int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM measurements")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Integer> getMeasurementCount();
    
    @androidx.room.Query(value = "SELECT * FROM measurements ORDER BY id DESC LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getLatestMeasurement(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.smarthas.data.database.Measurement> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM measurements ORDER BY id DESC LIMIT 1")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.smarthas.data.database.Measurement> getLatestMeasurementFlow();
}