package com.example.assignmen2cos30017.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Car data class implementing Parcelable manually for AGP 9.x / KGP 2.2.x compatibility.
 * Parcelable advantages over Serializable: faster (no reflection), no extra temporary objects,
 * recommended by Google for IPC and Activity transitions.
 */
data class Car(
    val id: Int,
    val name: String,
    val model: String,
    val year: Int,
    val rating: Float,
    val kilometres: Int,
    val dailyCost: Int,
    val imageResId: Int,
    val description: String,
    val engine: String,
    val seats: Int,
    val transmission: String,
    val fuel: String,
    val mileage: String,
    val color: String,
    var isBooked: Boolean = false,
    var isFavourite: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id           = parcel.readInt(),
        name         = parcel.readString() ?: "",
        model        = parcel.readString() ?: "",
        year         = parcel.readInt(),
        rating       = parcel.readFloat(),
        kilometres   = parcel.readInt(),
        dailyCost    = parcel.readInt(),
        imageResId   = parcel.readInt(),
        description  = parcel.readString() ?: "",
        engine       = parcel.readString() ?: "",
        seats        = parcel.readInt(),
        transmission = parcel.readString() ?: "",
        fuel         = parcel.readString() ?: "",
        mileage      = parcel.readString() ?: "",
        color        = parcel.readString() ?: "",
        isBooked     = parcel.readByte() != 0.toByte(),
        isFavourite  = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(model)
        parcel.writeInt(year)
        parcel.writeFloat(rating)
        parcel.writeInt(kilometres)
        parcel.writeInt(dailyCost)
        parcel.writeInt(imageResId)
        parcel.writeString(description)
        parcel.writeString(engine)
        parcel.writeInt(seats)
        parcel.writeString(transmission)
        parcel.writeString(fuel)
        parcel.writeString(mileage)
        parcel.writeString(color)
        parcel.writeByte(if (isBooked) 1 else 0)
        parcel.writeByte(if (isFavourite) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Car> {
        override fun createFromParcel(parcel: Parcel): Car = Car(parcel)
        override fun newArray(size: Int): Array<Car?> = arrayOfNulls(size)
    }
}

