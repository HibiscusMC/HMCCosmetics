package io.github.fisher2911.hmccosmetics.hook;

public class ModelEngineHook implements Hook {

    public ModelEngineHook() {
    }

    private static final String ID = "model-engine";

    @Override
    public String getId() {
        return ID;
    }
}
