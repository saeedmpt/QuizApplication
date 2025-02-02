package com.vesam.quiz.data.api;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 2}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\bf\u0018\u00002\u00020\u0001J/\u0010\u0002\u001a\u00020\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0006\u001a\u00020\u00052\b\b\u0001\u0010\u0007\u001a\u00020\bH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tJ/\u0010\n\u001a\u00020\u000b2\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0006\u001a\u00020\u00052\b\b\u0001\u0010\u0007\u001a\u00020\bH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tJ9\u0010\f\u001a\u00020\r2\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0006\u001a\u00020\u00052\b\b\u0001\u0010\u0007\u001a\u00020\b2\b\b\u0001\u0010\u000e\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u000f\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u0010"}, d2 = {"Lcom/vesam/quiz/data/api/ApiService;", "", "initGetQuizResult", "Lcom/vesam/quiz/data/model/get_quiz_result/ResponseGetQuizResultModel;", "userUuid", "", "userApiToken", "quizId", "", "(Ljava/lang/String;Ljava/lang/String;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "initQuizDetail", "Lcom/vesam/quiz/data/model/quiz_detail/ResponseQuizDetailModel;", "initSetQuizResult", "Lcom/vesam/quiz/data/model/set_quiz_result/ResponseSetQuizResultModel;", "userAnswer", "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "quiz_debug"})
public abstract interface ApiService {
    
    @org.jetbrains.annotations.Nullable()
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "quiz/get-quiz-with-details")
    public abstract java.lang.Object initQuizDetail(@org.jetbrains.annotations.NotNull()
    @retrofit2.http.Header(value = "user_uuid")
    java.lang.String userUuid, @org.jetbrains.annotations.NotNull()
    @retrofit2.http.Header(value = "user_api_token")
    java.lang.String userApiToken, @retrofit2.http.Field(value = "quiz_id")
    int quizId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vesam.quiz.data.model.quiz_detail.ResponseQuizDetailModel> p3);
    
    @org.jetbrains.annotations.Nullable()
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "quiz/set-quiz-result")
    public abstract java.lang.Object initSetQuizResult(@org.jetbrains.annotations.NotNull()
    @retrofit2.http.Header(value = "user_uuid")
    java.lang.String userUuid, @org.jetbrains.annotations.NotNull()
    @retrofit2.http.Header(value = "user_api_token")
    java.lang.String userApiToken, @retrofit2.http.Field(value = "quiz_id")
    int quizId, @org.jetbrains.annotations.NotNull()
    @retrofit2.http.Field(value = "user_answers")
    java.lang.String userAnswer, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vesam.quiz.data.model.set_quiz_result.ResponseSetQuizResultModel> p4);
    
    @org.jetbrains.annotations.Nullable()
    @retrofit2.http.POST(value = "quiz/get-quiz-result")
    public abstract java.lang.Object initGetQuizResult(@org.jetbrains.annotations.NotNull()
    @retrofit2.http.Header(value = "user_uuid")
    java.lang.String userUuid, @org.jetbrains.annotations.NotNull()
    @retrofit2.http.Header(value = "user_api_token")
    java.lang.String userApiToken, @retrofit2.http.Field(value = "quiz_id")
    int quizId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vesam.quiz.data.model.get_quiz_result.ResponseGetQuizResultModel> p3);
}