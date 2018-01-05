package dump.taptargetview;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Displays a sequence of {@link TapTargetView}s.
 * <p>
 * Internally, a FIFO queue is held to dictate which {@link TapTarget} will be shown.
 */
public class TapTargetSequence {
  private final @Nullable Activity activity;
  private final @Nullable Dialog dialog;
  private final Queue<TapTarget> targets;
  private boolean active;

  @Nullable
  private TapTargetView currentView;

  private Listener listener;
  private boolean considerOuterCircleCanceled;
  private boolean continueOnCancel;

  public interface Listener {
    /** Called when there are no more tap targets to display */
    void onSequenceFinish();

    /**
     * Called when moving onto the next tap target.
     * @param lastTarget The last displayed target
     * @param targetClicked Whether the last displayed target was clicked (this will always be true
     *                      unless you have set {@link #continueOnCancel(boolean)} and the user
     *                      clicks outside of the target
     */
    void onSequenceStep(TapTarget lastTarget, boolean targetClicked);

    /**
     * Called when the user taps outside of the current target, the target is cancelable, and
     * {@link #continueOnCancel(boolean)} is not set.
     * @param lastTarget The last displayed target
     */
    void onSequenceCanceled(TapTarget lastTarget);
  }

  public TapTargetSequence(@Nullable Activity activity) {
    if (activity == null) throw new IllegalArgumentException("Activity is null");
    this.activity = activity;
    this.dialog = null;
    this.targets = new LinkedList<>();
  }

  public TapTargetSequence(@Nullable Dialog dialog) {
    if (dialog == null) throw new IllegalArgumentException("Given null Dialog");
    this.dialog = dialog;
    this.activity = null;
    this.targets = new LinkedList<>();
  }

  /** Adds the given targets, in order, to the pending queue of {@link TapTarget}s */
  public TapTargetSequence targets(List<TapTarget> targets) {
    this.targets.addAll(targets);
    return this;
  }

  /** Adds the given targets, in order, to the pending queue of {@link TapTarget}s */
  public TapTargetSequence targets(TapTarget... targets) {
    Collections.addAll(this.targets, targets);
    return this;
  }

  /** Adds the given target to the pending queue of {@link TapTarget}s */
  public TapTargetSequence target(TapTarget target) {
    this.targets.add(target);
    return this;
  }

  /** Whether or not to continue the sequence when a {@link TapTarget} is canceled **/
  private TapTargetSequence continueOnCancel(boolean status) {
    this.continueOnCancel = status;
    return this;
  }

  /** Whether or not to consider taps on the outer circle as a cancellation **/
  public TapTargetSequence considerOuterCircleCanceled(boolean status) {
    this.considerOuterCircleCanceled = status;
    return this;
  }

  /** Specify the listener for this sequence **/
  public TapTargetSequence listener(Listener listener) {
    this.listener = listener;
    return this;
  }

  /** Immediately starts the sequence and displays the first target from the queue **/
  @UiThread
  public void start() {
    if (targets.isEmpty() || active) {
      return;
    }

    active = true;
    showNext();
  }

  /**
   * Cancels the sequence, if the current target is cancelable.
   * When the sequence is canceled, the current target is dismissed and the remaining targets are
   * removed from the sequence.
   * @return whether the sequence was canceled or not
   */
  @UiThread
  public boolean cancel() {
    if (targets.isEmpty() || !active) {
      return false;
    }
    if (currentView == null || !currentView.cancelable) {
      return false;
    }
    currentView.dismiss(false);
    active = false;
    targets.clear();
    if (listener != null) {
      listener.onSequenceCanceled(currentView.target);
    }
    return true;
  }

  private void showNext() {
    try {
      TapTarget tapTarget = targets.remove();
      if (activity != null) {
        currentView = TapTargetView.showFor(activity, tapTarget, tapTargetListener);
      } else {
        currentView = TapTargetView.showFor(dialog, tapTarget, tapTargetListener);
      }
    } catch (NoSuchElementException e) {
      // No more targets
      if (listener != null) {
        listener.onSequenceFinish();
      }
    }
  }

  private final TapTargetView.Listener tapTargetListener = new TapTargetView.Listener() {
    @Override
    public void onTargetClick(TapTargetView view) {
      super.onTargetClick(view);
      if (listener != null) {
        listener.onSequenceStep(view.target, true);
      }
      showNext();
    }

    @Override
    public void onOuterCircleClick(TapTargetView view) {
      if (considerOuterCircleCanceled) {
        onTargetCancel(view);
      }
    }

    @Override
    public void onTargetCancel(TapTargetView view) {
      super.onTargetCancel(view);
      if (continueOnCancel) {
        if (listener != null) {
          listener.onSequenceStep(view.target, false);
        }
        showNext();
      } else {
        if (listener != null) {
          listener.onSequenceCanceled(view.target);
        }
      }
    }
  };
}
