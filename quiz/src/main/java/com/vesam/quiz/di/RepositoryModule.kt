package com.vesam.quiz.di

import com.vesam.quiz.data.repository.QuizDetailDatabaseRepository
import com.vesam.quiz.data.repository.QuizRepository
import org.koin.dsl.module

val repoModule = module {

    single { QuizRepository(get()) }
    single { QuizDetailDatabaseRepository(get()) }

}