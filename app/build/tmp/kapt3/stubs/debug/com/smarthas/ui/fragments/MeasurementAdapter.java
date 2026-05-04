package com.smarthas.ui.fragments;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\f\u0012\b\u0012\u00060\u0002R\u00020\u00000\u0001:\u0001\u0015B\u001b\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\b\u0010\t\u001a\u00020\nH\u0016J\u001c\u0010\u000b\u001a\u00020\f2\n\u0010\r\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u000e\u001a\u00020\nH\u0016J\u001c\u0010\u000f\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\nH\u0016J\u0014\u0010\u0013\u001a\u00020\f2\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/smarthas/ui/fragments/MeasurementAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/smarthas/ui/fragments/MeasurementAdapter$MeasurementViewHolder;", "measurements", "", "Lcom/smarthas/data/database/Measurement;", "viewModel", "Lcom/smarthas/presentation/viewmodel/MeasurementViewModel;", "(Ljava/util/List;Lcom/smarthas/presentation/viewmodel/MeasurementViewModel;)V", "getItemCount", "", "onBindViewHolder", "", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "updateData", "newData", "MeasurementViewHolder", "app_debug"})
public final class MeasurementAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.smarthas.ui.fragments.MeasurementAdapter.MeasurementViewHolder> {
    @org.jetbrains.annotations.NotNull()
    private java.util.List<com.smarthas.data.database.Measurement> measurements;
    @org.jetbrains.annotations.NotNull()
    private final com.smarthas.presentation.viewmodel.MeasurementViewModel viewModel = null;
    
    public MeasurementAdapter(@org.jetbrains.annotations.NotNull()
    java.util.List<com.smarthas.data.database.Measurement> measurements, @org.jetbrains.annotations.NotNull()
    com.smarthas.presentation.viewmodel.MeasurementViewModel viewModel) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.smarthas.ui.fragments.MeasurementAdapter.MeasurementViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.smarthas.ui.fragments.MeasurementAdapter.MeasurementViewHolder holder, int position) {
    }
    
    @java.lang.Override()
    public int getItemCount() {
        return 0;
    }
    
    public final void updateData(@org.jetbrains.annotations.NotNull()
    java.util.List<com.smarthas.data.database.Measurement> newData) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b\u00a8\u0006\t"}, d2 = {"Lcom/smarthas/ui/fragments/MeasurementAdapter$MeasurementViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "itemView", "Landroid/view/View;", "(Lcom/smarthas/ui/fragments/MeasurementAdapter;Landroid/view/View;)V", "bind", "", "measurement", "Lcom/smarthas/data/database/Measurement;", "app_debug"})
    public final class MeasurementViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        
        public MeasurementViewHolder(@org.jetbrains.annotations.NotNull()
        android.view.View itemView) {
            super(null);
        }
        
        public final void bind(@org.jetbrains.annotations.NotNull()
        com.smarthas.data.database.Measurement measurement) {
        }
    }
}