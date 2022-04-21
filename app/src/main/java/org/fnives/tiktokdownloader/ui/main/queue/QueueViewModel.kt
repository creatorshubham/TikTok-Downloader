package org.fnives.tiktokdownloader.ui.main.queue

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.fnives.tiktokdownloader.data.model.VideoState
import org.fnives.tiktokdownloader.data.usecase.AddVideoToQueueUseCase
import org.fnives.tiktokdownloader.data.usecase.MoveVideoInQueue
import org.fnives.tiktokdownloader.data.usecase.RemoveVideoFromQueueUseCase
import org.fnives.tiktokdownloader.data.usecase.StateOfVideosObservableUseCase
import org.fnives.tiktokdownloader.data.usecase.VideoDownloadingProcessorUseCase
import org.fnives.tiktokdownloader.ui.shared.Event
import org.fnives.tiktokdownloader.ui.shared.asLiveData

class QueueViewModel(
    stateOfVideosObservableUseCase: StateOfVideosObservableUseCase,
    private val addVideoToQueueUseCase: AddVideoToQueueUseCase,
    private val removeVideoFromQueueUseCase: RemoveVideoFromQueueUseCase,
    private val videoDownloadingProcessorUseCase: VideoDownloadingProcessorUseCase,
    private val moveVideoInQueue: MoveVideoInQueue
) : ViewModel() {

    val downloads = asLiveData(stateOfVideosObservableUseCase())
    private val _navigationEvent = MutableLiveData<Event<NavigationEvent>>()
    val navigationEvent: LiveData<Event<NavigationEvent>> = _navigationEvent

    fun onSaveClicked(url: String) {
        addVideoToQueueUseCase(url)
        videoDownloadingProcessorUseCase.fetchVideoInState()
    }

    fun onItemClicked(path: String) {
        _navigationEvent.value = Event(NavigationEvent.OpenGallery(path))
    }

    fun onUrlClicked(url: String) {
        _navigationEvent.value = Event(NavigationEvent.OpenBrowser(url))
    }

    fun onElementDeleted(videoState: VideoState) {
        when (videoState) {
            is VideoState.InPending -> removeVideoFromQueueUseCase(videoState.videoInPending)
            is VideoState.Downloaded,
            is VideoState.InProcess -> Unit
        }
    }

    fun onElementMoved(moved: VideoState, to: VideoState): Boolean {
        if (moved !is VideoState.InPending) return false
        if (to !is VideoState.InPending) return false
        moveVideoInQueue(moved.videoInPending, to.videoInPending)
        return true
    }

    sealed class NavigationEvent {
        data class OpenBrowser(val url: String) : NavigationEvent()
        data class OpenGallery(val uri: String) : NavigationEvent()
    }
}