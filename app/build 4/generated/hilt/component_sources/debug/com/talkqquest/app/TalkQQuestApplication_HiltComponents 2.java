package com.talkqquest.app;

import com.talkqquest.app.core.di.NetworkModule;
import com.talkqquest.app.feature.archive.di.ArchiveModule;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveConversationDetailViewModel_HiltModules;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveHomeViewModel_HiltModules;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveReportViewModel_HiltModules;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSavedPhraseViewModel_HiltModules;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSearchViewModel_HiltModules;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveViewModel_HiltModules;
import com.talkqquest.app.feature.archive.viewmodel.ArchiveWeeklyCompareReportViewModel_HiltModules;
import com.talkqquest.app.feature.auth.di.AuthModule;
import com.talkqquest.app.feature.auth.viewmodel.AuthViewModel_HiltModules;
import com.talkqquest.app.feature.home.di.HomeModule;
import com.talkqquest.app.feature.home.viewmodel.HomeViewModel_HiltModules;
import com.talkqquest.app.feature.mission.di.MissionModule;
import com.talkqquest.app.feature.mission.viewmodel.ConversationViewModel_HiltModules;
import com.talkqquest.app.feature.mission.viewmodel.FeedbackDetailViewModel_HiltModules;
import com.talkqquest.app.feature.mission.viewmodel.FeedbackViewModel_HiltModules;
import com.talkqquest.app.feature.mission.viewmodel.MissionCompleteViewModel_HiltModules;
import com.talkqquest.app.feature.mission.viewmodel.MissionDetailViewModel_HiltModules;
import com.talkqquest.app.feature.mission.viewmodel.MissionListViewModel_HiltModules;
import com.talkqquest.app.feature.notification.di.NotificationModule;
import com.talkqquest.app.feature.notification.viewmodel.NotificationViewModel_HiltModules;
import com.talkqquest.app.feature.profile.viewmodel.ProfileViewModel_HiltModules;
import com.talkqquest.app.feature.report.di.ReportModule;
import com.talkqquest.app.feature.report.viewmodel.ReportViewModel_HiltModules;
import com.talkqquest.app.feature.report.viewmodel.WeeklyCompareViewModel_HiltModules;
import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Subcomponent;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.components.ActivityRetainedComponent;
import dagger.hilt.android.components.FragmentComponent;
import dagger.hilt.android.components.ServiceComponent;
import dagger.hilt.android.components.ViewComponent;
import dagger.hilt.android.components.ViewModelComponent;
import dagger.hilt.android.components.ViewWithFragmentComponent;
import dagger.hilt.android.flags.FragmentGetContextFix;
import dagger.hilt.android.flags.HiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory;
import dagger.hilt.android.internal.lifecycle.HiltWrapper_DefaultViewModelFactories_ActivityModule;
import dagger.hilt.android.internal.lifecycle.HiltWrapper_HiltViewModelFactory_ActivityCreatorEntryPoint;
import dagger.hilt.android.internal.lifecycle.HiltWrapper_HiltViewModelFactory_ViewModelModule;
import dagger.hilt.android.internal.managers.ActivityComponentManager;
import dagger.hilt.android.internal.managers.FragmentComponentManager;
import dagger.hilt.android.internal.managers.HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedComponentBuilderEntryPoint;
import dagger.hilt.android.internal.managers.HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedLifecycleEntryPoint;
import dagger.hilt.android.internal.managers.HiltWrapper_ActivityRetainedComponentManager_LifecycleModule;
import dagger.hilt.android.internal.managers.HiltWrapper_ActivitySavedStateHandleModule;
import dagger.hilt.android.internal.managers.ServiceComponentManager;
import dagger.hilt.android.internal.managers.ViewComponentManager;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.HiltWrapper_ActivityModule;
import dagger.hilt.android.scopes.ActivityRetainedScoped;
import dagger.hilt.android.scopes.ActivityScoped;
import dagger.hilt.android.scopes.FragmentScoped;
import dagger.hilt.android.scopes.ServiceScoped;
import dagger.hilt.android.scopes.ViewModelScoped;
import dagger.hilt.android.scopes.ViewScoped;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.internal.GeneratedComponent;
import dagger.hilt.migration.DisableInstallInCheck;
import javax.annotation.processing.Generated;
import javax.inject.Singleton;

@Generated("dagger.hilt.processor.internal.root.RootProcessor")
public final class TalkQQuestApplication_HiltComponents {
  private TalkQQuestApplication_HiltComponents() {
  }

  @Module(
      subcomponents = ServiceC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ServiceCBuilderModule {
    @Binds
    ServiceComponentBuilder bind(ServiceC.Builder builder);
  }

  @Module(
      subcomponents = ActivityRetainedC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ActivityRetainedCBuilderModule {
    @Binds
    ActivityRetainedComponentBuilder bind(ActivityRetainedC.Builder builder);
  }

  @Module(
      subcomponents = ActivityC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ActivityCBuilderModule {
    @Binds
    ActivityComponentBuilder bind(ActivityC.Builder builder);
  }

  @Module(
      subcomponents = ViewModelC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ViewModelCBuilderModule {
    @Binds
    ViewModelComponentBuilder bind(ViewModelC.Builder builder);
  }

  @Module(
      subcomponents = ViewC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ViewCBuilderModule {
    @Binds
    ViewComponentBuilder bind(ViewC.Builder builder);
  }

  @Module(
      subcomponents = FragmentC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface FragmentCBuilderModule {
    @Binds
    FragmentComponentBuilder bind(FragmentC.Builder builder);
  }

  @Module(
      subcomponents = ViewWithFragmentC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ViewWithFragmentCBuilderModule {
    @Binds
    ViewWithFragmentComponentBuilder bind(ViewWithFragmentC.Builder builder);
  }

  @Component(
      modules = {
          ApplicationContextModule.class,
          ArchiveModule.class,
          AuthModule.class,
          HiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule.class,
          HomeModule.class,
          MissionModule.class,
          NetworkModule.class,
          NotificationModule.class,
          ReportModule.class,
          ActivityRetainedCBuilderModule.class,
          ServiceCBuilderModule.class
      }
  )
  @Singleton
  @jakarta.inject.Singleton
  public abstract static class SingletonC implements TalkQQuestApplication_GeneratedInjector,
      FragmentGetContextFix.FragmentGetContextFixEntryPoint,
      HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedComponentBuilderEntryPoint,
      ServiceComponentManager.ServiceComponentBuilderEntryPoint,
      SingletonComponent,
      GeneratedComponent {
  }

  @Subcomponent
  @ServiceScoped
  public abstract static class ServiceC implements ServiceComponent,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ServiceComponentBuilder {
    }
  }

  @Subcomponent(
      modules = {
          ArchiveConversationDetailViewModel_HiltModules.KeyModule.class,
          ArchiveHomeViewModel_HiltModules.KeyModule.class,
          ArchiveReportViewModel_HiltModules.KeyModule.class,
          ArchiveSavedPhraseViewModel_HiltModules.KeyModule.class,
          ArchiveSearchViewModel_HiltModules.KeyModule.class,
          ArchiveViewModel_HiltModules.KeyModule.class,
          ArchiveWeeklyCompareReportViewModel_HiltModules.KeyModule.class,
          AuthViewModel_HiltModules.KeyModule.class,
          ConversationViewModel_HiltModules.KeyModule.class,
          FeedbackDetailViewModel_HiltModules.KeyModule.class,
          FeedbackViewModel_HiltModules.KeyModule.class,
          HiltWrapper_ActivityRetainedComponentManager_LifecycleModule.class,
          HiltWrapper_ActivitySavedStateHandleModule.class,
          HomeViewModel_HiltModules.KeyModule.class,
          MissionCompleteViewModel_HiltModules.KeyModule.class,
          MissionDetailViewModel_HiltModules.KeyModule.class,
          MissionListViewModel_HiltModules.KeyModule.class,
          NotificationViewModel_HiltModules.KeyModule.class,
          ProfileViewModel_HiltModules.KeyModule.class,
          ReportViewModel_HiltModules.KeyModule.class,
          ActivityCBuilderModule.class,
          ViewModelCBuilderModule.class,
          WeeklyCompareViewModel_HiltModules.KeyModule.class
      }
  )
  @ActivityRetainedScoped
  public abstract static class ActivityRetainedC implements ActivityRetainedComponent,
      ActivityComponentManager.ActivityComponentBuilderEntryPoint,
      HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedLifecycleEntryPoint,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ActivityRetainedComponentBuilder {
    }
  }

  @Subcomponent(
      modules = {
          HiltWrapper_ActivityModule.class,
          HiltWrapper_DefaultViewModelFactories_ActivityModule.class,
          FragmentCBuilderModule.class,
          ViewCBuilderModule.class
      }
  )
  @ActivityScoped
  public abstract static class ActivityC implements MainActivity_GeneratedInjector,
      ActivityComponent,
      DefaultViewModelFactories.ActivityEntryPoint,
      HiltWrapper_HiltViewModelFactory_ActivityCreatorEntryPoint,
      FragmentComponentManager.FragmentComponentBuilderEntryPoint,
      ViewComponentManager.ViewComponentBuilderEntryPoint,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ActivityComponentBuilder {
    }
  }

  @Subcomponent(
      modules = {
          ArchiveConversationDetailViewModel_HiltModules.BindsModule.class,
          ArchiveHomeViewModel_HiltModules.BindsModule.class,
          ArchiveReportViewModel_HiltModules.BindsModule.class,
          ArchiveSavedPhraseViewModel_HiltModules.BindsModule.class,
          ArchiveSearchViewModel_HiltModules.BindsModule.class,
          ArchiveViewModel_HiltModules.BindsModule.class,
          ArchiveWeeklyCompareReportViewModel_HiltModules.BindsModule.class,
          AuthViewModel_HiltModules.BindsModule.class,
          ConversationViewModel_HiltModules.BindsModule.class,
          FeedbackDetailViewModel_HiltModules.BindsModule.class,
          FeedbackViewModel_HiltModules.BindsModule.class,
          HiltWrapper_HiltViewModelFactory_ViewModelModule.class,
          HomeViewModel_HiltModules.BindsModule.class,
          MissionCompleteViewModel_HiltModules.BindsModule.class,
          MissionDetailViewModel_HiltModules.BindsModule.class,
          MissionListViewModel_HiltModules.BindsModule.class,
          NotificationViewModel_HiltModules.BindsModule.class,
          ProfileViewModel_HiltModules.BindsModule.class,
          ReportViewModel_HiltModules.BindsModule.class,
          WeeklyCompareViewModel_HiltModules.BindsModule.class
      }
  )
  @ViewModelScoped
  public abstract static class ViewModelC implements ViewModelComponent,
      HiltViewModelFactory.ViewModelFactoriesEntryPoint,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ViewModelComponentBuilder {
    }
  }

  @Subcomponent
  @ViewScoped
  public abstract static class ViewC implements ViewComponent,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ViewComponentBuilder {
    }
  }

  @Subcomponent(
      modules = ViewWithFragmentCBuilderModule.class
  )
  @FragmentScoped
  public abstract static class FragmentC implements FragmentComponent,
      DefaultViewModelFactories.FragmentEntryPoint,
      ViewComponentManager.ViewWithFragmentComponentBuilderEntryPoint,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends FragmentComponentBuilder {
    }
  }

  @Subcomponent
  @ViewScoped
  public abstract static class ViewWithFragmentC implements ViewWithFragmentComponent,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ViewWithFragmentComponentBuilder {
    }
  }
}
