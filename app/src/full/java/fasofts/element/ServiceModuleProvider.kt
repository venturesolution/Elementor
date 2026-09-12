package fasofts.element

import android.content.ContentResolver
import fasofts.element.data.DataModule
import fasofts.element.database.DatabaseModule
import fasofts.element.download.AppDownloadManager
import fasofts.element.scheduler.ScheduleManager
import fasofts.element.scheduler.WorkScheduler
import fasofts.element.service.AppUpdater
import fasofts.element.service.ServiceModule
import fasofts.element.util.Constants
import fasofts.element.util.OrbotHelper
import fasofts.element.viewmodel.ViewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/*
 * Copyright 2020 Element Powered by @fernandoangeli and its authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
private val rootModule = module { single<ContentResolver> { androidContext().contentResolver } }
private val updaterModule = module {
    single { NonStoreAppUpdater(Constants.RETHINK_APP_UPDATE_CHECK, get()) }
    single<AppUpdater> { get<NonStoreAppUpdater>() }
}

private val updaterModules = listOf(updaterModule)

private val orbotHelperModule = module { single { OrbotHelper(androidContext(), get(), get()) } }

private val appDownloadManagerModule = module {
    single { AppDownloadManager(androidContext(), get()) }
}

private val workerModule = module { single { WorkScheduler(androidContext()) } }

private val schedulerModule = module { single { ScheduleManager(androidContext()) } }

val AppModules: List<Module> by lazy {
    mutableListOf<Module>().apply {
        add(rootModule)
        addAll(DatabaseModule.modules)
        addAll(ViewModelModule.modules)
        addAll(DataModule.modules)
        addAll(ServiceModule.modules)
        addAll(updaterModules)
        add(schedulerModule)
        add(workerModule)
        add(orbotHelperModule)
        add(appDownloadManagerModule)
    }
}
