package com.artemzin.qualitymatters.ui.presenters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.artemzin.qualitymatters.models.ItemsModel;
import com.artemzin.qualitymatters.other.DisposableSubscription;
import com.artemzin.qualitymatters.performance.AsyncJob;
import com.artemzin.qualitymatters.performance.AsyncJobsObserver;
import com.artemzin.qualitymatters.ui.views.ItemsView;

import javax.inject.Inject;

import rx.Subscription;

public class ItemsPresenter extends Presenter<ItemsView> {

    @NonNull
    private final ItemsPresenterConfiguration presenterConfiguration;

    @NonNull
    private final ItemsModel itemsModel;

    @NonNull
    private final AsyncJobsObserver asyncJobsObserver;

    @Nullable
    private volatile ItemsView itemsView;

    @Inject
    public ItemsPresenter(@NonNull ItemsPresenterConfiguration presenterConfiguration,
                          @NonNull ItemsModel itemsModel,
                          @NonNull AsyncJobsObserver asyncJobsObserver) {
        this.presenterConfiguration = presenterConfiguration;
        this.itemsModel = itemsModel;
        this.asyncJobsObserver = asyncJobsObserver;
    }

    @Override
    public void bindView(@NonNull ItemsView view) {
        this.itemsView = view;
    }

    public void reloadData() {
        {
            // Tip: in Kotlin you can use ? to operate with nullable values.
            final ItemsView view = itemsView;

            if (view != null) {
                view.showLoadingUi();
            }
        }

        final AsyncJob asyncJob = asyncJobsObserver.asyncJobStarted("Reload data in ItemsPresenter");

        final Subscription subscription = itemsModel
                .getItems()
                .subscribeOn(presenterConfiguration.ioScheduler())
                .subscribe(
                        items -> {
                            // Tip: in Kotlin you can use ? to operate with nullable values.
                            final ItemsView view = itemsView;

                            if (view != null) {
                                view.showContentUi(items);
                            }

                            asyncJobsObserver.asyncJobFinished(asyncJob);
                        },
                        error -> {
                            // Tip: in Kotlin you can use ? to operate with nullable values.
                            final ItemsView view = itemsView;

                            if (view != null) {
                                view.showErrorUi(error);
                            }

                            asyncJobsObserver.asyncJobFinished(asyncJob);
                        }
                );

        // Prevent memory leak.
        unsubscribeOnUnbindView(subscription, new DisposableSubscription(() -> asyncJobsObserver.asyncJobFinished(asyncJob)));
    }
}
