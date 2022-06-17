package com.example.todoapp.fragments.countdown

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.todoapp.ui.CircularProgress
import com.example.todoapp.ui.theme.AppTheme
import com.example.todoapp.R

class CountDownFragment : Fragment() {

    private val step = 1000
    private val max = 100
    private val timeLiveData = MutableLiveData(max)
    private val time: LiveData<Int> = timeLiveData
    private val pausedLiveData = MutableLiveData(true)
    private val paused: LiveData<Boolean> = pausedLiveData
    private val timer = object : CountDownTimer(max * 1000L, step.toLong()) {
        override fun onTick(millisUntilFinished: Long) {
            val current = timeLiveData.value ?: 0
            if (current <= 0) {
                onFinish()
            } else {
                timeLiveData.value = current - 1
            }
        }

        override fun onFinish() {
            pausedLiveData.value = true
            timeLiveData.value = max
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    val timeValue by time.observeAsState(max)
                    val isPaused by paused.observeAsState(true)
                    CountdownScreen(timeValue, max, isPaused) {
                        when (it) {
                            Action.Play -> {
                                pausedLiveData.value = false
                                timer.start()
                            }
                            Action.Pause -> {
                                pausedLiveData.value = true
                                timer.cancel()
                            }
                            Action.Stop -> {
                                timer.cancel()
                                timer.onFinish()
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun CountdownScreen(time: Int, max: Int, paused: Boolean, onAction: (Action) -> Unit) {
    ConstraintLayout(Modifier.fillMaxSize()) {
        val (progress, label, btnStart, btnStop) = createRefs()
        CircularProgress(
            Modifier
                .width(150.dp)
                .height(150.dp)
                .constrainAs(progress) { centerTo(parent) },
            progress = time.toFloat() * 100 / max,
            color = MaterialTheme.colors.primary,
            animationSpec = tween(durationMillis = if (time == max) 2000 else 200, easing = LinearEasing)
        )
        Text(
            text = "$time",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.primary,
            fontSize = 50.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(8.dp)
                .constrainAs(label) { centerTo(progress) },
        )
        FloatingActionButton(
            onClick = { onAction.invoke(if (paused) Action.Play else Action.Pause) },
            modifier = Modifier
                .size(50.dp)
                .constrainAs(btnStart) {
                    top.linkTo(progress.top, 100.dp)
                    start.linkTo(progress.end, 10.dp)
                }
        ) {
            Image(
                painterResource(if (paused) R.drawable.ic_play else R.drawable.ic_pause),
                stringResource(if (paused) R.string.btn_play else R.string.btn_pause)
            )
        }
        FloatingActionButton(
            onClick = { onAction.invoke(Action.Stop) },
            modifier = Modifier
                .size(50.dp)
                .constrainAs(btnStop) {
                    top.linkTo(btnStart.bottom, 2.dp)
                    end.linkTo(btnStart.end, 40.dp)
                }
        ) {
            Image(painterResource(R.drawable.ic_stop), stringResource(R.string.btn_stop))
        }
    }
}

@Composable
@Preview
fun Mock() {
    AppTheme { CountdownScreen(70, 60, true, onAction = { }) }
}

sealed class Action {
    object Play : Action()
    object Pause : Action()
    object Stop : Action()
}
