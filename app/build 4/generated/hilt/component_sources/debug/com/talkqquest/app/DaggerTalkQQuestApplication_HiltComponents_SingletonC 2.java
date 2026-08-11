package com.talkqquest.app;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.talkqquest.app.core.datastore.TokenDataStore;
import com.talkqquest.app.core.datastore.TokenDataStore_Factory;
import com.talkqquest.app.core.datastore.UserXpStore;
import com.talkqquest.app.core.datastore.UserXpStore_Factory;
import com.talkqquest.app.core.di.NetworkModule_ProvideJsonFactory;
import com.talkqquest.app.core.di.NetworkModule_ProvideOkHttpClientFactory;
import com.talkqquest.app.core.di.NetworkModule_ProvideRetrofitFactory;
import com.talkqquest.app.core.network.AuthInterceptor;
import com.talkqquest.app.core.network.AuthInterceptor_Factory;
import com.talkqquest.app.core.network.TokenRefreshClient;
import com.talkqquest.app.core.network.TokenRefreshClient_Factory;
import com.talkqquest.app.feature.archive.data.ArchiveApi;
import com.talkqquest.app.feature.archive.data.ArchiveRepository;
import com.talkqquest.app.feature.archive.data.ArchiveRepository_Factory;
import com.talkqquest.app.feature.archive.di.ArchiveModule_ProvideArchiveApiFactory;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveConversationDetailViewModel;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveConversationDetailViewModel_Factory;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveConversationDetailViewModel_HiltModules;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveConversationDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveConversationDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveHomeViewModel;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveHomeViewModel_Factory;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveHomeViewModel_HiltModules;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveHomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveHomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveReportViewModel;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveReportViewModel_Factory;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveReportViewModel_HiltModules;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveReportViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveReportViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSavedPhraseViewModel;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSavedPhraseViewModel_Factory;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSavedPhraseViewModel_HiltModules;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSavedPhraseViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSavedPhraseViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSearchViewModel;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSearchViewModel_Factory;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSearchViewModel_HiltModules;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSearchViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSearchViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveViewModel;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveViewModel_Factory;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveViewModel_HiltModules;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveWeeklyCompareReportViewModel;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveWeeklyCompareReportViewModel_Factory;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveWeeklyCompareReportViewModel_HiltModules;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveWeeklyCompareReportViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveWeeklyCompareReportViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.talkqquest.app.feature.auth.data.AuthApi;
import com.talkqquest.app.feature.auth.data.AuthRepository;
import com.talkqquest.app.feature.auth.data.AuthRepository_Factory;
import com.talkqquest.app.feature.auth.di.AuthModule_ProvideAuthApiFactory;
import com.talkqquest.app.feature.auth.viewmodel.AuthViewModel;
import com.talkqquest.app.feature.auth.viewmodel.AuthViewModel_Factory;
import com.talkqquest.app.feature.auth.viewmodel.AuthViewModel_HiltModules;
import com.talkqquest.app.feature.auth.viewmodel.AuthViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.talkqquest.app.feature.auth.viewmodel.AuthViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.talkqquest.app.feature.home.data.HomeApi;
import com.talkqquest.app.feature.home.data.HomeRepository;
import com.talkqquest.app.feature.home.data.HomeRepository_Factory;
import com.talkqquest.app.feature.home.di.HomeModule_ProvideHomeApiFactory;
import com.talkqquest.app.feature.home.viewmodel.HomeViewModel;
import com.talkqquest.app.feature.home.viewmodel.HomeViewModel_Factory;
import com.talkqquest.app.feature.home.viewmodel.HomeViewModel_HiltModules;
import com.talkqquest.app.feature.home.viewmodel.HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.talkqquest.app.feature.home.viewmodel.HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.talkqquest.app.feature.mission.data.MissionApi;
import com.talkqquest.app.feature.mission.data.MissionRepository;
import com.talkqquest.app.feature.mission.data.MissionRepository_Factory;
import com.talkqquest.app.feature.mission.di.MissionModule_ProvideMissionApiFactory;
import com.talkqquest.app.feature.mission.viewmodel.ConversationViewModel;
import com.talkqquest.app.feature.mission.viewmodel.ConversationViewModel_Factory;
import com.talkqquest.app.feature.mission.viewmodel.ConversationViewModel_HiltModules;
import com.talkqquest.app.feature.mission.viewmodel.ConversationViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.talkqquest.app.feature.mission.viewmodel.ConversationViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.talkqquest.app.feature.mission.viewmodel.FeedbackDetailViewModel;
import com.talkqquest.app.feature.mission.viewmodel.FeedbackDetailViewModel_Factory;
import com.talkqquest.app.feature.mission.viewmodel.FeedbackDetailViewModel_HiltModules;
import com.talkqquest.app.feature.mission.viewmodel.FeedbackDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.talkqquest.app.feature.mission.viewmodel.FeedbackDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.talkqquest.app.feature.mission.viewmodel.FeedbackViewModel;
import com.talkqquest.app.feature.mission.viewmodel.FeedbackViewModel_Factory;
import com.talkqquest.app.feature.mission.viewmodel.FeedbackViewModel_HiltModules;
import com.talkqquest.app.feature.mission.viewmodel.FeedbackViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.talkqquest.app.feature.mission.viewmodel.FeedbackViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.talkqquest.app.feature.mission.viewmodel.MissionCompleteViewModel;
import com.talkqquest.app.feature.mission.viewmodel.MissionCompleteViewModel_Factory;
import com.talkqquest.app.feature.mission.viewmodel.MissionCompleteViewModel_HiltModules;
import com.talkqquest.app.feature.mission.viewmodel.MissionCompleteViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.talkqquest.app.feature.mission.viewmodel.MissionCompleteViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.talkqquest.app.feature.mission.viewmodel.MissionDetailViewModel;
import com.talkqquest.app.feature.mission.viewmodel.MissionDetailViewModel_Factory;
import com.talkqquest.app.feature.mission.viewmodel.MissionDetailViewModel_HiltModules;
import com.talkqquest.app.feature.mission.viewmodel.MissionDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.talkqquest.app.feature.mission.viewmodel.MissionDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.talkqquest.app.feature.mission.viewmodel.MissionListViewModel;
import com.talkqquest.app.feature.mission.viewmodel.MissionListViewModel_Factory;
import com.talkqquest.app.feature.mission.viewmodel.MissionListViewModel_HiltModules;
import com.talkqquest.app.feature.mission.viewmodel.MissionListViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.talkqquest.app.feature.mission.viewmodel.MissionListViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.talkqquest.app.feature.notification.data.NotificationApi;
import com.talkqquest.app.feature.notification.data.NotificationRepository;
import com.talkqquest.app.feature.notification.data.NotificationRepository_Factory;
import com.talkqquest.app.feature.notification.di.NotificationModule_ProvideNotificationApiFactory;
import com.talkqquest.app.feature.notification.viewmodel.NotificationViewModel;
import com.talkqquest.app.feature.notification.viewmodel.NotificationViewModel_Factory;
import com.talkqquest.app.feature.notification.viewmodel.NotificationViewModel_HiltModules;
import com.talkqquest.app.feature.notification.viewmodel.NotificationViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.talkqquest.app.feature.notification.viewmodel.NotificationViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.talkqquest.app.feature.profile.data.ProfileRepository;
import com.talkqquest.app.feature.profile.data.ProfileRepository_Factory;
import com.talkqquest.app.feature.profile.viewmodel.ProfileViewModel;
import com.talkqquest.app.feature.profile.viewmodel.ProfileViewModel_Factory;
import com.talkqquest.app.feature.profile.viewmodel.ProfileViewModel_HiltModules;
import com.talkqquest.app.feature.profile.viewmodel.ProfileViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.talkqquest.app.feature.profile.viewmodel.ProfileViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.talkqquest.app.feature.report.data.ReportApi;
import com.talkqquest.app.feature.report.data.ReportRepository;
import com.talkqquest.app.feature.report.data.ReportRepository_Factory;
import com.talkqquest.app.feature.report.data.WeeklyCompareRepository;
import com.talkqquest.app.feature.report.data.WeeklyCompareRepository_Factory;
import com.talkqquest.app.feature.report.di.ReportModule_ProvideReportApiFactory;
import com.talkqquest.app.feature.report.viewmodel.ReportViewModel;
import com.talkqquest.app.feature.report.viewmodel.ReportViewModel_Factory;
import com.talkqquest.app.feature.report.viewmodel.ReportViewModel_HiltModules;
import com.talkqquest.app.feature.report.viewmodel.ReportViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.talkqquest.app.feature.report.viewmodel.ReportViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.talkqquest.app.feature.report.viewmodel.WeeklyCompareViewModel;
import com.talkqquest.app.feature.report.viewmodel.WeeklyCompareViewModel_Factory;
import com.talkqquest.app.feature.report.viewmodel.WeeklyCompareViewModel_HiltModules;
import com.talkqquest.app.feature.report.viewmodel.WeeklyCompareViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.talkqquest.app.feature.report.viewmodel.WeeklyCompareViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.InstanceFactory;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import kotlinx.serialization.json.Json;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class DaggerTalkQQuestApplication_HiltComponents_SingletonC {
  private DaggerTalkQQuestApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public TalkQQuestApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements TalkQQuestApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public TalkQQuestApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements TalkQQuestApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public TalkQQuestApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements TalkQQuestApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public TalkQQuestApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements TalkQQuestApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public TalkQQuestApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements TalkQQuestApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public TalkQQuestApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements TalkQQuestApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public TalkQQuestApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements TalkQQuestApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public TalkQQuestApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends TalkQQuestApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends TalkQQuestApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    FragmentCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends TalkQQuestApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends TalkQQuestApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    ActivityCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    Map keySetMapOfClassOfObjectAndBooleanBuilder() {
      MapBuilder mapBuilder = MapBuilder.<String, Boolean>newMapBuilder(19);
      mapBuilder.put(ArchiveConversationDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ArchiveConversationDetailViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(ArchiveHomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ArchiveHomeViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(ArchiveReportViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ArchiveReportViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(ArchiveSavedPhraseViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ArchiveSavedPhraseViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(ArchiveSearchViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ArchiveSearchViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(ArchiveViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ArchiveViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(ArchiveWeeklyCompareReportViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ArchiveWeeklyCompareReportViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(AuthViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AuthViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(ConversationViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ConversationViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(FeedbackDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, FeedbackDetailViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(FeedbackViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, FeedbackViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HomeViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(MissionCompleteViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, MissionCompleteViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(MissionDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, MissionDetailViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(MissionListViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, MissionListViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(NotificationViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, NotificationViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(ProfileViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ProfileViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(ReportViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ReportViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(WeeklyCompareViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, WeeklyCompareViewModel_HiltModules.KeyModule.provide());
      return mapBuilder.build();
    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(keySetMapOfClassOfObjectAndBooleanBuilder());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }
  }

  private static final class ViewModelCImpl extends TalkQQuestApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    Provider<SavedStateHandle> savedStateHandleProvider;

    Provider<ArchiveConversationDetailViewModel> archiveConversationDetailViewModelProvider;

    Provider<ArchiveHomeViewModel> archiveHomeViewModelProvider;

    Provider<ArchiveReportViewModel> archiveReportViewModelProvider;

    Provider<ArchiveSavedPhraseViewModel> archiveSavedPhraseViewModelProvider;

    Provider<ArchiveSearchViewModel> archiveSearchViewModelProvider;

    Provider<ArchiveViewModel> archiveViewModelProvider;

    Provider<ArchiveWeeklyCompareReportViewModel> archiveWeeklyCompareReportViewModelProvider;

    Provider<AuthRepository> authRepositoryProvider;

    Provider<AuthViewModel> authViewModelProvider;

    Provider<ConversationViewModel> conversationViewModelProvider;

    Provider<FeedbackDetailViewModel> feedbackDetailViewModelProvider;

    Provider<FeedbackViewModel> feedbackViewModelProvider;

    Provider<HomeRepository> homeRepositoryProvider;

    Provider<HomeViewModel> homeViewModelProvider;

    Provider<MissionCompleteViewModel> missionCompleteViewModelProvider;

    Provider<MissionDetailViewModel> missionDetailViewModelProvider;

    Provider<MissionListViewModel> missionListViewModelProvider;

    Provider<NotificationViewModel> notificationViewModelProvider;

    Provider<ProfileRepository> profileRepositoryProvider;

    Provider<ProfileViewModel> profileViewModelProvider;

    Provider<ReportViewModel> reportViewModelProvider;

    Provider<WeeklyCompareViewModel> weeklyCompareViewModelProvider;

    ViewModelCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        SavedStateHandle savedStateHandleParam, ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    Map hiltViewModelMapMapOfClassOfObjectAndProviderOfViewModelBuilder() {
      MapBuilder mapBuilder = MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(19);
      mapBuilder.put(ArchiveConversationDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (archiveConversationDetailViewModelProvider)));
      mapBuilder.put(ArchiveHomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (archiveHomeViewModelProvider)));
      mapBuilder.put(ArchiveReportViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (archiveReportViewModelProvider)));
      mapBuilder.put(ArchiveSavedPhraseViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (archiveSavedPhraseViewModelProvider)));
      mapBuilder.put(ArchiveSearchViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (archiveSearchViewModelProvider)));
      mapBuilder.put(ArchiveViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (archiveViewModelProvider)));
      mapBuilder.put(ArchiveWeeklyCompareReportViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (archiveWeeklyCompareReportViewModelProvider)));
      mapBuilder.put(AuthViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (authViewModelProvider)));
      mapBuilder.put(ConversationViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (conversationViewModelProvider)));
      mapBuilder.put(FeedbackDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (feedbackDetailViewModelProvider)));
      mapBuilder.put(FeedbackViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (feedbackViewModelProvider)));
      mapBuilder.put(HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (homeViewModelProvider)));
      mapBuilder.put(MissionCompleteViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (missionCompleteViewModelProvider)));
      mapBuilder.put(MissionDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (missionDetailViewModelProvider)));
      mapBuilder.put(MissionListViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (missionListViewModelProvider)));
      mapBuilder.put(NotificationViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (notificationViewModelProvider)));
      mapBuilder.put(ProfileViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (profileViewModelProvider)));
      mapBuilder.put(ReportViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (reportViewModelProvider)));
      mapBuilder.put(WeeklyCompareViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (weeklyCompareViewModelProvider)));
      return mapBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.savedStateHandleProvider = InstanceFactory.create(savedStateHandleParam);
      this.archiveConversationDetailViewModelProvider = ArchiveConversationDetailViewModel_Factory.create(singletonCImpl.archiveRepositoryProvider, savedStateHandleProvider);
      this.archiveHomeViewModelProvider = ArchiveHomeViewModel_Factory.create(singletonCImpl.archiveRepositoryProvider);
      this.archiveReportViewModelProvider = ArchiveReportViewModel_Factory.create(singletonCImpl.archiveRepositoryProvider, savedStateHandleProvider);
      this.archiveSavedPhraseViewModelProvider = ArchiveSavedPhraseViewModel_Factory.create(singletonCImpl.archiveRepositoryProvider, savedStateHandleProvider);
      this.archiveSearchViewModelProvider = ArchiveSearchViewModel_Factory.create(singletonCImpl.archiveRepositoryProvider);
      this.archiveViewModelProvider = ArchiveViewModel_Factory.create(singletonCImpl.archiveRepositoryProvider);
      this.archiveWeeklyCompareReportViewModelProvider = ArchiveWeeklyCompareReportViewModel_Factory.create(singletonCImpl.archiveRepositoryProvider, savedStateHandleProvider);
      this.authRepositoryProvider = AuthRepository_Factory.create(singletonCImpl.provideAuthApiProvider, singletonCImpl.tokenDataStoreProvider, singletonCImpl.provideHomeApiProvider);
      this.authViewModelProvider = AuthViewModel_Factory.create(authRepositoryProvider);
      this.conversationViewModelProvider = ConversationViewModel_Factory.create(singletonCImpl.missionRepositoryProvider, savedStateHandleProvider);
      this.feedbackDetailViewModelProvider = FeedbackDetailViewModel_Factory.create(singletonCImpl.missionRepositoryProvider, savedStateHandleProvider);
      this.feedbackViewModelProvider = FeedbackViewModel_Factory.create(singletonCImpl.missionRepositoryProvider, savedStateHandleProvider);
      this.homeRepositoryProvider = HomeRepository_Factory.create(singletonCImpl.provideHomeApiProvider, singletonCImpl.provideMissionApiProvider, singletonCImpl.provideNotificationApiProvider, singletonCImpl.userXpStoreProvider);
      this.homeViewModelProvider = HomeViewModel_Factory.create(homeRepositoryProvider);
      this.missionCompleteViewModelProvider = MissionCompleteViewModel_Factory.create(singletonCImpl.missionRepositoryProvider, savedStateHandleProvider);
      this.missionDetailViewModelProvider = MissionDetailViewModel_Factory.create(singletonCImpl.missionRepositoryProvider, savedStateHandleProvider);
      this.missionListViewModelProvider = MissionListViewModel_Factory.create(singletonCImpl.missionRepositoryProvider);
      this.notificationViewModelProvider = NotificationViewModel_Factory.create(singletonCImpl.notificationRepositoryProvider);
      this.profileRepositoryProvider = ProfileRepository_Factory.create(singletonCImpl.provideHomeApiProvider);
      this.profileViewModelProvider = ProfileViewModel_Factory.create(profileRepositoryProvider);
      this.reportViewModelProvider = ReportViewModel_Factory.create(singletonCImpl.reportRepositoryProvider, savedStateHandleProvider);
      this.weeklyCompareViewModelProvider = WeeklyCompareViewModel_Factory.create(singletonCImpl.weeklyCompareRepositoryProvider);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(hiltViewModelMapMapOfClassOfObjectAndProviderOfViewModelBuilder());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }
  }

  private static final class ActivityRetainedCImpl extends TalkQQuestApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.create());
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }
  }

  private static final class ServiceCImpl extends TalkQQuestApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends TalkQQuestApplication_HiltComponents.SingletonC {
    private final SingletonCImpl singletonCImpl = this;

    Provider<Context> provideContextProvider;

    Provider<TokenDataStore> tokenDataStoreProvider;

    Provider<TokenRefreshClient> tokenRefreshClientProvider;

    Provider<AuthInterceptor> authInterceptorProvider;

    Provider<OkHttpClient> provideOkHttpClientProvider;

    Provider<Json> provideJsonProvider;

    Provider<Retrofit> provideRetrofitProvider;

    Provider<ArchiveApi> provideArchiveApiProvider;

    Provider<ArchiveRepository> archiveRepositoryProvider;

    Provider<AuthApi> provideAuthApiProvider;

    Provider<HomeApi> provideHomeApiProvider;

    Provider<MissionApi> provideMissionApiProvider;

    Provider<UserXpStore> userXpStoreProvider;

    Provider<MissionRepository> missionRepositoryProvider;

    Provider<NotificationApi> provideNotificationApiProvider;

    Provider<NotificationRepository> notificationRepositoryProvider;

    Provider<ReportApi> provideReportApiProvider;

    Provider<ReportRepository> reportRepositoryProvider;

    Provider<WeeklyCompareRepository> weeklyCompareRepositoryProvider;

    SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {

      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideContextProvider = ApplicationContextModule_ProvideContextFactory.create(applicationContextModuleParam);
      this.tokenDataStoreProvider = DoubleCheck.provider(TokenDataStore_Factory.create(provideContextProvider));
      this.tokenRefreshClientProvider = DoubleCheck.provider(TokenRefreshClient_Factory.create(tokenDataStoreProvider));
      this.authInterceptorProvider = AuthInterceptor_Factory.create(tokenDataStoreProvider, tokenRefreshClientProvider);
      this.provideOkHttpClientProvider = DoubleCheck.provider(NetworkModule_ProvideOkHttpClientFactory.create(authInterceptorProvider));
      this.provideJsonProvider = DoubleCheck.provider(NetworkModule_ProvideJsonFactory.create());
      this.provideRetrofitProvider = DoubleCheck.provider(NetworkModule_ProvideRetrofitFactory.create(provideOkHttpClientProvider, provideJsonProvider));
      this.provideArchiveApiProvider = DoubleCheck.provider(ArchiveModule_ProvideArchiveApiFactory.create(provideRetrofitProvider));
      this.archiveRepositoryProvider = DoubleCheck.provider(ArchiveRepository_Factory.create(provideArchiveApiProvider));
      this.provideAuthApiProvider = DoubleCheck.provider(AuthModule_ProvideAuthApiFactory.create(provideRetrofitProvider));
      this.provideHomeApiProvider = DoubleCheck.provider(HomeModule_ProvideHomeApiFactory.create(provideRetrofitProvider));
      this.provideMissionApiProvider = DoubleCheck.provider(MissionModule_ProvideMissionApiFactory.create(provideRetrofitProvider));
      this.userXpStoreProvider = DoubleCheck.provider(UserXpStore_Factory.create());
      this.missionRepositoryProvider = DoubleCheck.provider(MissionRepository_Factory.create(provideMissionApiProvider, provideHomeApiProvider, userXpStoreProvider));
      this.provideNotificationApiProvider = DoubleCheck.provider(NotificationModule_ProvideNotificationApiFactory.create(provideRetrofitProvider));
      this.notificationRepositoryProvider = DoubleCheck.provider(NotificationRepository_Factory.create(provideNotificationApiProvider));
      this.provideReportApiProvider = DoubleCheck.provider(ReportModule_ProvideReportApiFactory.create(provideRetrofitProvider));
      this.reportRepositoryProvider = DoubleCheck.provider(ReportRepository_Factory.create(provideReportApiProvider));
      this.weeklyCompareRepositoryProvider = DoubleCheck.provider(WeeklyCompareRepository_Factory.create(provideReportApiProvider));
    }

    @Override
    public void injectTalkQQuestApplication(TalkQQuestApplication arg0) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }
  }
}
