package com.example.tugas_firebase.database

import android.os.Parcel
import android.os.Parcelable

data class Report(
    var id: String = "",
    var nama: String = "",
    var judul: String = "",
    var isi: String = ""
): Parcelable {
    // Parcelable implementation
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nama)
        parcel.writeString(judul)
        parcel.writeString(isi)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Report> {
        override fun createFromParcel(parcel: Parcel): Report {
            return Report(parcel)
        }

        override fun newArray(size: Int): Array<Report?> {
            return arrayOfNulls(size)
        }
    }

    private constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        nama = parcel.readString() ?: "",
        judul = parcel.readString() ?: "",
        isi = parcel.readString() ?: ""
    )
}
