package com.coolightman.note.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.coolightman.note.domain.entity.Note
import com.coolightman.note.domain.entity.SortNoteBy
import com.coolightman.note.domain.repository.NoteRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotesViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val handler = CoroutineExceptionHandler { _, throwable ->
        Log.e("Coroutine_exception", throwable.stackTraceToString())
    }

    private val _sortNoteBy = MutableLiveData<SortNoteBy>()

    val notes: LiveData<List<Note>> = liveData {
        val sortedNotes = Transformations.switchMap(_sortNoteBy) {
            repository.getAllNotes(it)
        }
        emitSource(sortedNotes)
    }

    val trashCount: LiveData<Int> = repository.getTrashCount()

    fun setSortBy(sortBy: SortNoteBy) {
        _sortNoteBy.postValue(sortBy)
    }

    fun showDate(showDate: Boolean) {
        viewModelScope.launch(Dispatchers.IO + handler) {
            repository.showDate(showDate)
        }
    }

    fun sendToTrashBasket(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO + handler) {
            repository.sendToTrashBasket(noteId)
        }
    }
}