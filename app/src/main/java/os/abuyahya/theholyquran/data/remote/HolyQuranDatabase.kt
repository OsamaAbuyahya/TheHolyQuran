package os.abuyahya.theholyquran.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import os.abuyahya.theholyquran.data.entites.Surah
import os.abuyahya.theholyquran.other.Constants.HOLY_QURAN_COLLECTION

class HolyQuranDatabase {

    private val firestore = FirebaseFirestore.getInstance()
    private val holyQuranCollection = firestore.collection(HOLY_QURAN_COLLECTION)

    suspend fun getAllSurahs(): List<Surah> {
        return try {
            holyQuranCollection.get().await().toObjects(Surah::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
