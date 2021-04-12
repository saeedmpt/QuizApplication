package com.vesam.quiz.ui.view.fragment


import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.danikula.videocache.HttpProxyCacheServer
import com.vesam.quiz.data.model.quiz_detail.Answer
import com.vesam.quiz.data.model.quiz_detail.Question
import com.vesam.quiz.data.model.quiz_detail.ResponseQuizDetailModel
import com.vesam.quiz.databinding.FragmentQuestionsBinding
import com.vesam.quiz.interfaces.OnClickListenerAny
import com.vesam.quiz.ui.view.adapter.answer_list.AnswerAdapter
import com.vesam.quiz.ui.viewmodel.QuizViewModel
import com.vesam.quiz.utils.application.AppQuiz
import com.vesam.quiz.utils.build_config.BuildConfig
import com.vesam.quiz.utils.build_config.BuildConfig.Companion.FORMAT_AUDIO
import com.vesam.quiz.utils.build_config.BuildConfig.Companion.FORMAT_TEXT
import com.vesam.quiz.utils.build_config.BuildConfig.Companion.FORMAT_VIDEO
import com.vesam.quiz.utils.build_config.BuildConfig.Companion.HOW_DISPLAY_CORRECT_ANSWER
import com.vesam.quiz.utils.build_config.BuildConfig.Companion.STEP_BY_STEP
import com.vesam.quiz.utils.extention.checkPersianCharacter
import com.vesam.quiz.utils.extention.getProxy
import com.vesam.quiz.utils.extention.initTick
import com.vesam.quiz.utils.tools.GlideTools
import com.vesam.quiz.utils.tools.HandelErrorTools
import com.vesam.quiz.utils.tools.ThrowableTools
import com.vesam.quiz.utils.tools.ToastTools
import org.koin.android.ext.android.inject


class QuestionsFragment : Fragment() {
    private lateinit var binding: FragmentQuestionsBinding

    // sound
    private lateinit var mediaPlayerQuestion: MediaPlayer
    private lateinit var mediaPlayerAnswer: MediaPlayer

    private val navController: NavController by inject()
    private val toastTools: ToastTools by inject()
    private val glideTools: GlideTools by inject()
    private val throwableTools: ThrowableTools by inject()
    private val handelErrorTools: HandelErrorTools by inject()
    private val quizViewModel: QuizViewModel by inject()
    private val answerAdapter: AnswerAdapter by inject()
    private val questionList: ArrayList<Question> = ArrayList()
    private lateinit var timer: CountDownTimer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuestionsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            initAction()
        } catch (e: Exception) {
            handelErrorTools.handelError(e)
        }
    }

    override fun onResume() {
        super.onResume()
        initResumeVideo()
    }

    override fun onPause() {
        super.onPause()
        initPauseVideo()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMPQuestion()
        releaseMPAnswer()
    }

    private fun initAction() {
        initAdapter()
        initOnClick()
        initRequestListQuiz()
        initOnBackPress()
    }

    private fun initOnClick() {
        binding.btnNextQuestion.setOnClickListener { initCheckQuestion() }
        binding.lnQuestionSoundLayout.imgQuestionPlaySound.setOnClickListener { initPlaySoundQuestion() }
        binding.lnQuestionSoundLayout.imgQuestionPauseSound.setOnClickListener { initPauseSoundQuestion() }
        binding.lnAnswerSoundLayout.imgAnswerPlaySound.setOnClickListener { initPlaySoundAnswer() }
        binding.lnAnswerSoundLayout.imgAnswerPauseSound.setOnClickListener { initPauseSoundAnswer() }
    }

    private fun initRequestListQuiz() {
        quizViewModel.initQuizDetail(
            BuildConfig.USER_UUID_VALUE,
            BuildConfig.USER_API_TOKEN_VALUE,
            BuildConfig.USER_QUIZ_ID_VALUE
        ).observe(
            requireActivity(),
            this::initResultListQuiz
        )
    }

    private fun initResultListQuiz(it: Any) {
        when (it) {
            is ResponseQuizDetailModel -> initQuizDetailModel(it)
            is Throwable -> initThrowable(it)
        }
    }

    private fun initQuizDetailModel(it: ResponseQuizDetailModel) {
        questionList.addAll(it.questions)
        initHowDisplayCorrectAnswer(it)
        initQuestion()
    }

    private fun initHowDisplayCorrectAnswer(it: ResponseQuizDetailModel) {
        HOW_DISPLAY_CORRECT_ANSWER = it.details.howDisplayCorrectAnswer
    }

    private fun initCheckQuestion() {
        questionList.let {
            when {
                it.isEmpty() -> initResult()
                else -> initQuestion()
            }
        }
    }

    private fun initResult() {
        toastTools.toast("result")
    }

    private fun initQuestion() {
        val question: Question = questionList.first()
        questionList.removeFirst()
        initCancelTimer()
        initStopVideo()
        initHideAllAnswer()
        initStateQuestionFormat(question)
        checkPersianCharacter(question.title, binding.lnQuestionTextLayout.txtQuestion)
        initHideAnswerQuestion()
        answerAdapter.updateList(question.answers)
    }

    private fun initHideAnswerQuestion() {
        binding.btnNextQuestion.visibility = View.GONE
        binding.lnAnswerSoundLayout.lnAnswerSound.visibility = View.GONE
        binding.lnAnswerVideoLayout.cvAnswerVideo.visibility = View.GONE
    }

    private fun initStateQuestionFormat(question: Question) {
        when (question.quizDescription.format) {
            FORMAT_TEXT -> initQuestionFormatText(question)
            FORMAT_VIDEO -> initQuestionFormatVideo(question)
            FORMAT_AUDIO -> initQuestionFormatAudio(question)
            else -> initQuestionFormatImage(question)
        }
    }


    private fun initQuestionFormatImage(question: Question) {
        initShowQuestionFormatImage()
        initPeriodImageTime(question)
        glideTools.displayImageOriginal(
            binding.lnQuestionImageLayout.imgQuestion,
            question.quizDescription.urlContent
        )
    }

    private fun initQuestionFormatAudio(question: Question) {
        binding.btnNextQuestion.visibility = View.VISIBLE
        initShowQuestionFormatSound()
        initPeriodSoundTime(question)
        initSoundQuestion(question.quizDescription.urlContent)
    }

    private fun initSoundQuestion(content: String) {
        val proxy: HttpProxyCacheServer = getProxy(requireContext())
        val proxyUrl = proxy.getProxyUrl(content)
        releaseMPQuestion()
        mediaPlayerQuestion = MediaPlayer()
        try {
            mediaPlayerQuestion.setDataSource(requireContext(), Uri.parse(proxyUrl))
            mediaPlayerQuestion.prepare()
            mediaPlayerQuestion.prepareAsync()
        } catch (e: Exception) {
            handelErrorTools.handelError(e)
        }
        initPlaySoundQuestion()
    }

    private fun releaseMPQuestion() {
        if (::mediaPlayerQuestion.isInitialized) try {
            mediaPlayerQuestion.release()
        } catch (e: Exception) {
            handelErrorTools.handelError(e)
        }
    }

    private fun releaseMPAnswer() {
        if (::mediaPlayerAnswer.isInitialized) try {
            mediaPlayerAnswer.release()
        } catch (e: Exception) {
            handelErrorTools.handelError(e)
        }
    }

    private fun initPlaySoundQuestion() {
        binding.lnQuestionSoundLayout.imgQuestionPlaySound.visibility = View.GONE
        binding.lnQuestionSoundLayout.imgQuestionPauseSound.visibility = View.VISIBLE
        mediaPlayerQuestion.start()
    }

    private fun initPauseSoundQuestion() {
        binding.lnQuestionSoundLayout.imgQuestionPlaySound.visibility = View.VISIBLE
        binding.lnQuestionSoundLayout.imgQuestionPauseSound.visibility = View.GONE
        mediaPlayerQuestion.pause()
    }

    private fun initPlaySoundAnswer() {
        binding.lnAnswerSoundLayout.imgAnswerPlaySound.visibility = View.VISIBLE
        binding.lnAnswerSoundLayout.imgAnswerPauseSound.visibility = View.GONE
        mediaPlayerAnswer.start()
    }

    private fun initPauseSoundAnswer() {
        binding.lnAnswerSoundLayout.imgAnswerPlaySound.visibility = View.GONE
        binding.lnAnswerSoundLayout.imgAnswerPauseSound.visibility = View.VISIBLE
        mediaPlayerAnswer.pause()
    }

    private fun initQuestionFormatVideo(question: Question) {
        binding.btnNextQuestion.visibility = View.VISIBLE
        initShowQuestionFormatVideo()
        initPeriodVideoTime(question)
        initVideoQuestion(question.quizDescription.urlContent)
    }

    private fun initVideoQuestion(content: String) {
        val proxy: HttpProxyCacheServer = getProxy(requireContext())
        val proxyUrl = proxy.getProxyUrl(content)
        binding.lnQuestionVideoLayout.viewVideoQuestion.setVideoPath(proxyUrl)
        val mediaController = MediaController(requireContext())
        binding.lnQuestionVideoLayout.viewVideoQuestion.setMediaController(mediaController)
        mediaController.setAnchorView(binding.lnQuestionVideoLayout.viewVideoQuestion)
    }

    private fun initQuestionFormatText(question: Question) {
        initShowQuestionFormatText()
        initPeriodTextTime(question)
        binding.lnQuestionTextLayout.txtQuestion.text = question.title
    }

    private fun initShowQuestionFormatText() {
        binding.lnQuestionVideoLayout.cvQuestionVideo.visibility = View.GONE
        binding.lnQuestionTextLayout.lnText.visibility = View.VISIBLE
        binding.lnQuestionImageLayout.lnImage.visibility = View.GONE
        binding.lnQuestionSoundLayout.lnSound.visibility = View.GONE
    }

    private fun initShowQuestionFormatImage() {
        binding.lnQuestionVideoLayout.cvQuestionVideo.visibility = View.GONE
        binding.lnQuestionImageLayout.lnImage.visibility = View.VISIBLE
        binding.lnQuestionSoundLayout.lnSound.visibility = View.GONE
        binding.lnQuestionTextLayout.lnText.visibility = View.GONE
    }

    private fun initShowQuestionFormatSound() {
        binding.lnQuestionVideoLayout.cvQuestionVideo.visibility = View.GONE
        binding.lnQuestionSoundLayout.lnSound.visibility = View.VISIBLE
        binding.lnQuestionImageLayout.lnImage.visibility = View.GONE
        binding.lnQuestionTextLayout.lnText.visibility = View.GONE
    }

    private fun initShowQuestionFormatVideo() {
        binding.lnQuestionVideoLayout.cvQuestionVideo.visibility = View.VISIBLE
        binding.lnQuestionSoundLayout.lnSound.visibility = View.GONE
        binding.lnQuestionImageLayout.lnImage.visibility = View.GONE
        binding.lnQuestionTextLayout.lnText.visibility = View.GONE
    }

    private fun initPeriodTextTime(question: Question) {
        binding.lnQuestionTextLayout.progressPeriodTextTime.max = question.periodTime
        binding.lnQuestionTextLayout.progressPeriodTextTime.progress = question.periodTime
        timer = object : CountDownTimer((question.periodTime * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) =
                initTick(millisUntilFinished, binding.lnQuestionTextLayout.progressPeriodTextTime)

            override fun onFinish() = initFinish()
        }
        timer.start()
    }

    private fun initPeriodImageTime(question: Question) {
        binding.lnQuestionImageLayout.progressPeriodImageTime.max = question.periodTime
        binding.lnQuestionImageLayout.progressPeriodImageTime.progress = question.periodTime
        timer = object : CountDownTimer((question.periodTime * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) = initTick(
                millisUntilFinished,
                binding.lnQuestionImageLayout.progressPeriodImageTime
            )

            override fun onFinish() = initFinish()
        }
        timer.start()
    }

    private fun initPeriodVideoTime(question: Question) {
        binding.lnQuestionVideoLayout.progressPeriodVideoTime.max = question.periodTime
        binding.lnQuestionVideoLayout.progressPeriodVideoTime.progress = question.periodTime
        timer = object : CountDownTimer((question.periodTime * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) = initTick(
                millisUntilFinished,
                binding.lnQuestionVideoLayout.progressPeriodVideoTime
            )

            override fun onFinish() = initFinish()
        }
        timer.start()
    }

    private fun initPeriodSoundTime(question: Question) {
        binding.lnQuestionSoundLayout.progressPeriodSoundTime.max = question.periodTime
        binding.lnQuestionSoundLayout.progressPeriodSoundTime.progress = question.periodTime
        timer = object : CountDownTimer((question.periodTime * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) = initTick(
                millisUntilFinished,
                binding.lnQuestionSoundLayout.progressPeriodSoundTime
            )

            override fun onFinish() = initFinish()
        }
        timer.start()
    }

    private fun initFinish() = iniResultAnswerFinish(answerAdapter.initFindIsCorrectAnswer()!!)

    private fun iniResultAnswerFinish(it: Answer) = initAnswerFinish(it)

    private fun initAnswerFinish(it: Answer) {
        answerAdapter.disableClick()
        initStateListFormat(it)
    }

    private fun initThrowable(it: Throwable) {
        val message = throwableTools.getThrowableError(it)
        handelErrorTools.handelError(it)
        toastTools.toast(message)
    }

    private fun initAdapter() {
        binding.rcQuestion.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rcQuestion.setHasFixedSize(true)
        binding.rcQuestion.adapter = answerAdapter
        answerAdapter.setOnItemClickListener(object : OnClickListenerAny {
            override fun onClickListener(any: Any) = initOnItemClick(any)
        })
    }

    private fun initOnItemClick(any: Any) {
        val answer: Answer = any as Answer
        when (HOW_DISPLAY_CORRECT_ANSWER) {
            STEP_BY_STEP -> initStepByStep(answer)
            else -> initStateListFormat(answer)
        }

    }

    private fun initStepByStep(answer: Answer) = when (answer.isCorrect) {
        1 -> initSuccessAnswer(answer)
        else -> initUnSuccessAnswer(answer)
    }

    private fun initSuccessAnswer(answer: Answer) {
        answerAdapter.answerSuccessQuestion(answer)
        initStateListFormat(answer)
    }

    private fun initUnSuccessAnswer(answer: Answer) {
        val isCorrectAnswer = answerAdapter.initFindIsCorrectAnswer()
        answerAdapter.answerUnSuccessQuestion(answer)
        initStateListFormat(isCorrectAnswer!!)
    }

    private fun initStateListFormat(answer: Answer) = when (answer.description.format) {
        FORMAT_TEXT -> initListFormatText(answer)
        FORMAT_VIDEO -> initListFormatVideo(answer)
        FORMAT_AUDIO -> initListFormatSound(answer)
        else -> initListFormatImage(answer)
    }

    private fun initHideAllAnswer() {
        binding.lnAnswerImageLayout.lnAnswerImage.visibility = View.GONE
        binding.lnAnswerVideoLayout.cvAnswerVideo.visibility = View.GONE
        binding.lnAnswerSoundLayout.lnAnswerSound.visibility = View.GONE
        binding.lnAnswerTextLayout.lnAnswerText.visibility = View.GONE
    }

    private fun initShowAnswerFormatText() {
        binding.lnAnswerImageLayout.imgAnswer.visibility = View.GONE
        binding.lnAnswerVideoLayout.cvAnswerVideo.visibility = View.GONE
        binding.lnAnswerTextLayout.lnAnswerText.visibility = View.VISIBLE
        binding.lnAnswerSoundLayout.lnAnswerSound.visibility = View.GONE
    }

    private fun initShowAnswerFormatImage() {
        binding.lnAnswerImageLayout.lnAnswerImage.visibility = View.VISIBLE
        binding.lnAnswerVideoLayout.cvAnswerVideo.visibility = View.GONE
        binding.lnAnswerSoundLayout.lnAnswerSound.visibility = View.GONE
        binding.lnAnswerTextLayout.lnAnswerText.visibility = View.GONE
    }

    private fun initShowAnswerFormatSound() {
        binding.lnAnswerImageLayout.lnAnswerImage.visibility = View.GONE
        binding.lnAnswerVideoLayout.cvAnswerVideo.visibility = View.GONE
        binding.lnAnswerSoundLayout.lnAnswerSound.visibility = View.VISIBLE
        binding.lnAnswerTextLayout.lnAnswerText.visibility = View.GONE
    }

    private fun initShowAnswerFormatVideo() {
        binding.lnAnswerImageLayout.lnAnswerImage.visibility = View.GONE
        binding.lnAnswerVideoLayout.cvAnswerVideo.visibility = View.VISIBLE
        binding.lnAnswerSoundLayout.lnAnswerSound.visibility = View.GONE
        binding.lnAnswerTextLayout.lnAnswerText.visibility = View.GONE
    }

    private fun initListFormatText(answer: Answer) {
        binding.btnNextQuestion.visibility = View.VISIBLE
        binding.lnAnswerTextLayout.txtTextAnswer.text = answer.description.content
        initShowAnswerFormatText()
        initCancelTimer()
    }

    private fun initListFormatVideo(answer: Answer) {
        binding.btnNextQuestion.visibility = View.VISIBLE
        initShowAnswerFormatVideo()
        initVideoView(answer.description.urlContent)
        initCancelTimer()
    }

    private fun initVideoView(content: String) {
        val proxy: HttpProxyCacheServer = getProxy(requireContext())
        val proxyUrl = proxy.getProxyUrl(content)
        binding.lnAnswerVideoLayout.viewVideo.setVideoPath(proxyUrl)
        val mediaController = MediaController(requireContext())
        binding.lnAnswerVideoLayout.viewVideo.setMediaController(mediaController)
        mediaController.setAnchorView(binding.lnAnswerVideoLayout.viewVideo)
    }

    private fun initStopVideo() {
        if (binding.lnAnswerVideoLayout.viewVideo.isPlaying)
            binding.lnAnswerVideoLayout.viewVideo.stopPlayback()
    }

    private fun initPauseVideo() {
        if (binding.lnAnswerVideoLayout.viewVideo.isPlaying)
            binding.lnAnswerVideoLayout.viewVideo.pause()
    }

    private fun initResumeVideo() {
        binding.lnAnswerVideoLayout.viewVideo.resume()
    }

    private fun initListFormatSound(answer: Answer) {
        binding.btnNextQuestion.visibility = View.VISIBLE
        initShowAnswerFormatSound()
        initSoundAnswer(answer.description.urlContent)
        initCancelTimer()
    }

    private fun initListFormatImage(answer: Answer) {
        binding.btnNextQuestion.visibility = View.VISIBLE
        initShowAnswerFormatImage()
        initCancelTimer()
        glideTools.displayImageOriginal(
            binding.lnAnswerImageLayout.imgAnswer,
            answer.description.urlContent
        )
    }

    private fun initSoundAnswer(content: String) {
        val proxy: HttpProxyCacheServer = getProxy(requireContext())
        val proxyUrl = proxy.getProxyUrl(content)
        releaseMPAnswer()
        try {
            mediaPlayerAnswer = MediaPlayer()
            mediaPlayerAnswer.setDataSource(requireContext(), Uri.parse(proxyUrl))
            mediaPlayerQuestion.prepare()
            mediaPlayerQuestion.prepareAsync()
        } catch (e: Exception) {
            handelErrorTools.handelError(e)
        }
    }


    private fun initOnBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = initOnBackPressed()
            })
    }

    private fun initOnBackPressed() {
        initCancelTimer()
        AppQuiz.activity.finish()
    }

    private fun initCancelTimer() {
        if (this::timer.isInitialized)
            timer.cancel()
    }
}