package com.smarthas.presentation.viewmodel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\"\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u00192\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u001cJ\u000e\u0010\u001d\u001a\u00020\u00172\u0006\u0010\u001e\u001a\u00020\u0019J\u0016\u0010\u001f\u001a\u00020 2\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u0019J\u0006\u0010!\u001a\u00020\u0017R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\t0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0019\u0010\u000e\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u00140\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0012\u00a8\u0006\""}, d2 = {"Lcom/smarthas/presentation/viewmodel/MeasurementViewModel;", "Landroidx/lifecycle/ViewModel;", "measurementRepository", "Lcom/smarthas/data/repository/MeasurementRepository;", "authRepository", "Lcom/smarthas/data/repository/AuthRepository;", "(Lcom/smarthas/data/repository/MeasurementRepository;Lcom/smarthas/data/repository/AuthRepository;)V", "_createState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/smarthas/presentation/viewmodel/CreateMeasurementState;", "createState", "Lkotlinx/coroutines/flow/StateFlow;", "getCreateState", "()Lkotlinx/coroutines/flow/StateFlow;", "latestMeasurement", "Lkotlinx/coroutines/flow/Flow;", "Lcom/smarthas/data/database/Measurement;", "getLatestMeasurement", "()Lkotlinx/coroutines/flow/Flow;", "measurements", "", "getMeasurements", "createMeasurement", "", "systolic", "", "diastolic", "notes", "", "deleteMeasurement", "id", "getClassification", "Lcom/smarthas/presentation/viewmodel/BloodPressureClassification;", "resetCreateState", "app_debug"})
public final class MeasurementViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.smarthas.data.repository.MeasurementRepository measurementRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.smarthas.data.repository.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.util.List<com.smarthas.data.database.Measurement>> measurements = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<com.smarthas.data.database.Measurement> latestMeasurement = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.smarthas.presentation.viewmodel.CreateMeasurementState> _createState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.smarthas.presentation.viewmodel.CreateMeasurementState> createState = null;
    
    public MeasurementViewModel(@org.jetbrains.annotations.NotNull()
    com.smarthas.data.repository.MeasurementRepository measurementRepository, @org.jetbrains.annotations.NotNull()
    com.smarthas.data.repository.AuthRepository authRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.smarthas.data.database.Measurement>> getMeasurements() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.smarthas.data.database.Measurement> getLatestMeasurement() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.smarthas.presentation.viewmodel.CreateMeasurementState> getCreateState() {
        return null;
    }
    
    public final void createMeasurement(int systolic, int diastolic, @org.jetbrains.annotations.Nullable()
    java.lang.String notes) {
    }
    
    public final void deleteMeasurement(int id) {
    }
    
    public final void resetCreateState() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.smarthas.presentation.viewmodel.BloodPressureClassification getClassification(int systolic, int diastolic) {
        return null;
    }
}