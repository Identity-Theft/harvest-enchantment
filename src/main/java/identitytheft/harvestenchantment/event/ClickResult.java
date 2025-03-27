package identitytheft.harvestenchantment.event;

import net.minecraft.util.ActionResult;

public record ClickResult(Boolean hasValue) {

    private static final ClickResult INTERRUPT = new ClickResult(true);
    private static final ClickResult PASS = new ClickResult(null);

    public static ClickResult pass() {
        return PASS;
    }

    public static ClickResult interrupt() {
        return INTERRUPT;
    }

    public boolean isPresent() {
        return hasValue != null;
    }

    public ActionResult getInteractionResult() {
        if (isPresent()) {
            return hasValue() ? ActionResult.SUCCESS : ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

}
