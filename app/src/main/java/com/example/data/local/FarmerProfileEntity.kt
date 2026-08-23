package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.models.CropType
import com.example.models.FarmerProfile

@Entity(tableName = "farmer_profiles")
data class FarmerProfileEntity(
    @PrimaryKey val id: String = "FARMER-8842",
    val fullName: String = "Rameshwar Singh",
    val registrationId: String = "MFMB-2026-987410",
    val phone: String = "+91 98765 43210",
    val aadhaarLast4: String = "8842",
    val preferredCentreId: String = "centre-krn-01",
    val preferredCentreName: String = "Central Procurement Mandi - Karnal",
    val village: String = "Nilokheri",
    val block: String = "Karnal Sadar",
    val district: String = "Karnal",
    val state: String = "Haryana",
    val landRecordNo: String = "Khasra #412/18",
    val landAreaAcres: Double = 8.5,
    val primaryCrop: String = "WHEAT",
    val expectedQuantityQuintals: Double = 120.0,
    val bankName: String = "State Bank of India",
    val bankAccountLast4: String = "5412",
    val ifscCode: String = "SBIN0001234",
    val preferredLanguage: String = "ENGLISH",
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): FarmerProfile {
        val crop = try {
            CropType.valueOf(primaryCrop)
        } catch (_: Exception) {
            CropType.WHEAT
        }
        return FarmerProfile(
            id = id,
            fullName = fullName,
            phone = phone,
            aadhaarLast4 = aadhaarLast4,
            village = village,
            block = block,
            district = district,
            state = state,
            landRecordNo = landRecordNo,
            landAreaAcres = landAreaAcres,
            primaryCrop = crop,
            expectedQuantityQuintals = expectedQuantityQuintals,
            registrationId = registrationId,
            preferredCentreId = preferredCentreId,
            preferredCentreName = preferredCentreName,
            bankName = bankName,
            bankAccountLast4 = bankAccountLast4,
            ifscCode = ifscCode
        )
    }

    companion object {
        fun fromDomainModel(profile: FarmerProfile): FarmerProfileEntity {
            return FarmerProfileEntity(
                id = profile.id,
                fullName = profile.fullName,
                registrationId = profile.registrationId,
                phone = profile.phone,
                aadhaarLast4 = profile.aadhaarLast4,
                preferredCentreId = profile.preferredCentreId,
                preferredCentreName = profile.preferredCentreName,
                village = profile.village,
                block = profile.block,
                district = profile.district,
                state = profile.state,
                landRecordNo = profile.landRecordNo,
                landAreaAcres = profile.landAreaAcres,
                primaryCrop = profile.primaryCrop.name,
                expectedQuantityQuintals = profile.expectedQuantityQuintals,
                bankName = profile.bankName,
                bankAccountLast4 = profile.bankAccountLast4,
                ifscCode = profile.ifscCode,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
