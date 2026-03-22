package com.example.myapplication.di

import com.example.myapplication.data.repository.OcrRepository
import com.example.myapplication.data.repository.TranslationRepository
import com.example.myapplication.ui.TranslatorViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { OcrRepository(androidContext()) }
    single { TranslationRepository() }
    viewModelOf(::TranslatorViewModel)
}

