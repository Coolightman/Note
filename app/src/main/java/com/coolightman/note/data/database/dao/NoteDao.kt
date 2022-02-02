package com.coolightman.note.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.coolightman.note.data.database.dbModel.NoteDb

@Dao
interface NoteDao {

    @Insert(onConflict = REPLACE)
    suspend fun insert(vararg note: NoteDb)

    @Query("SELECT * FROM notedb ORDER BY color")
    fun getAll(): LiveData<List<NoteDb>>

    @Query("SELECT * FROM notedb WHERE noteId = :noteId")
    suspend fun get(noteId: Long): NoteDb

    @Query("DELETE FROM notedb WHERE noteId = :noteId")
    suspend fun delete(noteId: Long)
}