package net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy;

public class RuleType {
    private ActionsType actions;
    private ConditionsType conditions;
    private String id;
    private TransformationsType transformations;

    public ConditionsType getConditions() {
        return this.conditions;
    }

    public void setConditions(ConditionsType conditions) {
        this.conditions = conditions;
    }

    public ActionsType getActions() {
        return this.actions;
    }

    public void setActions(ActionsType actions) {
        this.actions = actions;
    }

    public TransformationsType getTransformations() {
        return this.transformations;
    }

    public void setTransformations(TransformationsType transformations) {
        this.transformations = transformations;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
