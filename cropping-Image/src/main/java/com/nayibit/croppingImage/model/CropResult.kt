package com.nayibit.croppingImage.model

// coordinates is [points] flattened in topLeft, topRight, bottomLeft, bottomRight order (x,y pairs)
data class CropResult(
    val points: Map<CropCorner, Point>,
    val coordinates: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CropResult) return false
        return points == other.points && coordinates.contentEquals(other.coordinates)
    }

    override fun hashCode(): Int {
        return 31 * points.hashCode() + coordinates.contentHashCode()
    }
}
