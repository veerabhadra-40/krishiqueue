package com.example.ui.components

import com.example.models.FarmerProfile

object KrishiChatKnowledgeBase {

    data class BotResponse(
        val englishText: String,
        val hindiText: String,
        val category: String,
        val suggestedQuestions: List<String> = emptyList(),
        val hindiSuggestedQuestions: List<String> = emptyList()
    )

    fun answerQuery(query: String, farmer: FarmerProfile): BotResponse {
        val q = query.lowercase().trim()

        return when {
            // 1. MSP Queries
            q.contains("msp") || q.contains("rate") || q.contains("price") || q.contains("भाव") || q.contains("दाम") || q.contains("मूल्य") || q.contains("रेट") -> {
                when {
                    q.contains("wheat") || q.contains("गेहूं") -> BotResponse(
                        englishText = "🌾 **Government MSP for Wheat (2025-26):**\n• Rate: **₹2,275 per Quintal** (₹22.75/kg)\n• Permissible Moisture: Up to **12.0%** (Max 14.0% with standard dockage)\n• DBT Payment Timeline: Direct credit within **48–72 hours** of weighbridge entry.",
                        hindiText = "🌾 **गेहूं का सरकारी न्यूनतम समर्थन मूल्य (MSP 2025-26):**\n• सरकारी दर: **₹2,275 प्रति क्विंटल**\n• स्वीकृत नमी मानक: अधिकतम **12.0%**\n• डीबीटी भुगतान: तौल के बाद **48 से 72 घंटों** में सीधे आधार लिंक बैंक खाते में।",
                        category = "MSP",
                        suggestedQuestions = listOf("What is Mustard MSP?", "How to check moisture?", "How to book wheat slot?"),
                        hindiSuggestedQuestions = listOf("सरसों का एमएसपी क्या है?", "नमी कैसे जांचें?", "गेहूं का स्लॉट कैसे बुक करें?")
                    )
                    q.contains("mustard") || q.contains("सरसों") || q.contains("sarson") -> BotResponse(
                        englishText = "🌼 **Government MSP for Mustard / Rapeseed:**\n• Rate: **₹5,650 per Quintal**\n• Permissible Moisture: Up to **8.0%** (Clean & dry seed norm)\n• Direct procurement active at Karnal, Kurukshetra & Hisar mandis.",
                        hindiText = "🌼 **सरसों का सरकारी समर्थन मूल्य (MSP):**\n• सरकारी दर: **₹5,650 प्रति क्विंटल**\n• स्वीकृत नमी मानक: अधिकतम **8.0%**\n• करनाल, कुरुक्षेत्र व हिसार मंडियों में सीधी खरीद चालू है।",
                        category = "MSP",
                        suggestedQuestions = listOf("Mustard moisture limit?", "How to book slot?"),
                        hindiSuggestedQuestions = listOf("सरसों में नमी की सीमा?", "स्लॉट कैसे बुक करें?")
                    )
                    q.contains("paddy") || q.contains("धान") || q.contains("dhan") || q.contains("rice") || q.contains("चावल") -> BotResponse(
                        englishText = "🌾 **Government MSP for Paddy (Dhan):**\n• Common Grade: **₹2,183 per Quintal**\n• Grade-A Paddy: **₹2,203 per Quintal**\n• Permissible Moisture: Up to **17.0%**.",
                        hindiText = "🌾 **धान (Paddy) का सरकारी न्यूनतम समर्थन मूल्य:**\n• सामान्य ग्रेड: **₹2,183 प्रति क्विंटल**\n• ग्रेड-ए धान: **₹2,203 प्रति क्विंटल**\n• स्वीकृत नमी मानक: अधिकतम **17.0%**।",
                        category = "MSP",
                        suggestedQuestions = listOf("What is Wheat MSP?", "What is Mustard MSP?"),
                        hindiSuggestedQuestions = listOf("गेहूं का भाव क्या है?", "सरसों का भाव क्या है?")
                    )
                    else -> BotResponse(
                        englishText = "📊 **Current Government MSP Rates (2025-26):**\n• **Wheat (गेहूं):** ₹2,275 / Qtl\n• **Mustard (सरसों):** ₹5,650 / Qtl\n• **Gram / Chana (चना):** ₹5,440 / Qtl\n• **Barley (जौ):** ₹1,850 / Qtl\n• **Paddy (धान):** ₹2,183 / Qtl\n\nAll payments are 100% credited to your Aadhaar-linked DBT account.",
                        hindiText = "📊 **वर्तमान सरकारी समर्थन मूल्य (MSP 2025-26):**\n• **गेहूं (Wheat):** ₹2,275 प्रति क्विंटल\n• **सरसों (Mustard):** ₹5,650 प्रति क्विंटल\n• **चना (Gram):** ₹5,440 प्रति क्विंटल\n• **जौ (Barley):** ₹1,850 प्रति क्विंटल\n• **धान (Paddy):** ₹2,183 प्रति क्विंटल\n\nसभी भुगतान सीधे आपके बैंक खाते में डीबीटी द्वारा भेजे जाते हैं।",
                        category = "MSP",
                        suggestedQuestions = listOf("How to book a slot?", "Wheat moisture limit?"),
                        hindiSuggestedQuestions = listOf("स्लॉट कैसे बुक करें?", "गेहूं में नमी मानक?")
                    )
                }
            }

            // 2. Slot Booking Queries
            q.contains("book") || q.contains("slot") || q.contains("booking") || q.contains("स्लॉट") || q.contains("बुकिंग") || q.contains("टोकन") || q.contains("token") -> {
                BotResponse(
                    englishText = "📱 **How to Book a Procurement Slot:**\n1. Go to **Book Slot** in bottom navigation or dashboard.\n2. Choose your preferred Mandi (e.g. ${farmer.preferredCentreName}).\n3. Select your desired delivery date & 1-hour time window.\n4. Enter estimated crop quantity (Max registered: ${farmer.expectedQuantityQuintals} Qtl).\n5. Click **Confirm Booking** to receive your instant digital Token with QR code pass!",
                    hindiText = "📱 **मंडी खरीद स्लॉट बुक करने का तरीका:**\n1. नीचे दिए गए मेन्यू में **स्लॉट बुक करें** पर टैप करें।\n2. अपनी नजदीकी मंडी चुनें (उदा. ${farmer.preferredCentreName})।\n3. मनपसंद तारीख और 1 घंटे का समय स्लॉट चुनें।\n4. फसल की मात्रा दर्ज करें (पंजीकृत सीमा: ${farmer.expectedQuantityQuintals} क्विंटल)।\n5. **बुकिंग कन्फर्म करें** दबाएं और तुरंत डिजिटल टोकन व क्यूआर गेट-पास प्राप्त करें!",
                    category = "SLOT",
                    suggestedQuestions = listOf("My token status?", "Required documents?", "Mandi open timings?"),
                    hindiSuggestedQuestions = listOf("मेरा टोकन स्टेटस क्या है?", "आवश्यक दस्तावेज?", "मंडी खुलने का समय?")
                )
            }

            // 3. Queue & Wait Times Queries
            q.contains("queue") || q.contains("wait") || q.contains("line") || q.contains("कतार") || q.contains("प्रतीक्षा") || q.contains("समय") || q.contains("नंबर") || q.contains("बारी") -> {
                BotResponse(
                    englishText = "🚜 **Live Mandi Queue Information:**\n• Your registered farmer token: **CP-104**\n• Center: **${farmer.preferredCentreName}**\n• Status: Real-time electronic queue is active.\n• Average turnaround: **~6.5 minutes per trolley**.\n• When you are 1 position away, KrishiQueue sends an automatic **⚡ YOU ARE NEXT** push alert and SMS so you can drive your tractor to Gate 1 without delay.",
                    hindiText = "🚜 **लाइव मंडी कतार जानकारी:**\n• आपका पंजीकृत टोकन: **CP-104**\n• मंडी केंद्र: **${farmer.preferredCentreName}**\n• कतार स्थिति: रीयल-टाइम इलेक्ट्रॉनिक कतार सक्रिय है।\n• औसत समय: **~6.5 मिनट प्रति ट्रॉली**।\n• जब आपकी बारी से पहले केवल 1 किसान होगा, तो ऐप स्वतः **⚡ आप अगले हैं** का अलर्ट और एसएमएस भेजेगा।",
                    category = "QUEUE",
                    suggestedQuestions = listOf("How to get next alert?", "Operator counter list?"),
                    hindiSuggestedQuestions = listOf("अलर्ट कैसे मिलेगा?", "काउंटर सूची देखें?")
                )
            }

            // 4. Moisture Testing & Quality Norms
            q.contains("moisture") || q.contains("quality") || q.contains("नमी") || q.contains("गुणवत्ता") || q.contains("जांच") || q.contains("टेस्ट") -> {
                BotResponse(
                    englishText = "⚖️ **Grain Quality & Moisture Inspection Standards:**\n• **Wheat:** Max permissible moisture is **12.0%** (12.1% - 14% subject to standard value cut).\n• **Mustard:** Max moisture is **8.0%**.\n• **Foreign matter limit:** Maximum 0.75%.\n• **Tip:** Dry your harvest on clean tarpaulin before bringing to mandi to avoid any price deduction!",
                    hindiText = "⚖️ **अनाज गुणवत्ता एवं नमी मानक (Moisture Norms):**\n• **गेहूं:** मानक नमी **12.0%** तक मान्य (12.1% से 14% तक आंशिक कटौती)।\n• **सरसों:** मानक नमी **8.0%** तक मान्य।\n• **कचरा / धूल मानक:** अधिकतम 0.75%।\n• **सलाह:** मंडी लाने से पहले फसल को साफ तिरपाल पर अच्छी तरह सुखाएं ताकि पूरा एमएसपी मूल्य मिले!",
                    category = "MOISTURE",
                    suggestedQuestions = listOf("What is Wheat MSP?", "Required documents?"),
                    hindiSuggestedQuestions = listOf("गेहूं का एमएसपी?", "आवश्यक दस्तावेज?")
                )
            }

            // 5. Payment & DBT Queries
            q.contains("payment") || q.contains("dbt") || q.contains("money") || q.contains("bank") || q.contains("रुपये") || q.contains("भुगतान") || q.contains("पैसा") || q.contains("खाता") -> {
                BotResponse(
                    englishText = "💳 **Direct Benefit Transfer (DBT) Payment Details:**\n• Your registered bank: **${farmer.bankName} (A/C ****${farmer.bankAccountLast4})**\n• Payment time: **48 to 72 hours** from electronic weighment slip generation.\n• Money is transferred directly via PFMS portal to your Aadhaar-seeded bank account.\n• Digital J-Form & E-Receipt are downloadable in the **History & Receipts** section.",
                    hindiText = "💳 **डीबीटी (DBT) बैंक भुगतान की जानकारी:**\n• आपका पंजीकृत बैंक: **${farmer.bankName} (खाता संख्या ****${farmer.bankAccountLast4})**\n• भुगतान समय: इलेक्ट्रॉनिक तौल रसीद जारी होने के **48 से 72 घंटों** के भीतर।\n• राशि सीधे आधार-लिंक्ड बैंक खाते में पीएफएमएस द्वारा भेजी जाती है।\n• डिजिटल जे-फॉर्म व रसीद आप **इतिहास व रसीदें** टैब से डाउनलोड कर सकते हैं।",
                    category = "PAYMENT",
                    suggestedQuestions = listOf("View my receipts", "How to update bank details?"),
                    hindiSuggestedQuestions = listOf("मेरी रसीदें देखें", "बैंक खाता कैसे अपडेट करें?")
                )
            }

            // 6. Documents Required
            q.contains("doc") || q.contains("paper") || q.contains("dastavez") || q.contains("दस्तावेज") || q.contains("कागजात") || q.contains("आईडी") || q.contains("aadhaar") || q.contains("आधार") -> {
                BotResponse(
                    englishText = "📋 **Mandatory Documents for Mandi Entry:**\n1. **Digital Slot Token QR Pass** (shown on this app).\n2. **Farmer Registration ID** (${farmer.registrationId}).\n3. **Aadhaar Card** (Original or DigiLocker).\n4. **Bank Passbook Copy** (with IFSC & Aadhaar link).\n5. **Land Record (Fard / Jamabandi / Khasra No: ${farmer.landRecordNo})**.",
                    hindiText = "📋 **मंडी प्रवेश हेतु आवश्यक दस्तावेज:**\n1. **डिजिटल स्लॉट टोकन क्यूआर पास** (इस ऐप में उपलब्ध)।\n2. **किसान पंजीकरण संख्या** (${farmer.registrationId})।\n3. **आधार कार्ड** (मूल प्रति या डिजिलॉकर)।\n4. **बैंक पासबुक प्रति** (आधार से जुड़ा खाता)।\n5. **जमाबंदी / फर्द / खसरा संख्या (${farmer.landRecordNo})**।",
                    category = "GATE",
                    suggestedQuestions = listOf("How to book a slot?", "What is current Wheat MSP?"),
                    hindiSuggestedQuestions = listOf("स्लॉट कैसे बुक करें?", "गेहूं का एमएसपी क्या है?")
                )
            }

            // 7. Profile & Registration Details
            q.contains("profile") || q.contains("my details") || q.contains("नाम") || q.contains("पंजीकरण") || q.contains("registration") || q.contains("id") -> {
                BotResponse(
                    englishText = "👤 **Your Saved Farmer Profile:**\n• **Name:** ${farmer.fullName}\n• **Reg ID:** ${farmer.registrationId}\n• **Phone:** ${farmer.phone}\n• **Preferred Mandi:** ${farmer.preferredCentreName}\n• **Land:** ${farmer.landAreaAcres} Acres in ${farmer.village}, ${farmer.district}\n• **Primary Crop:** ${farmer.primaryCrop.englishName} (~${farmer.expectedQuantityQuintals} Qtl)\n\nYou can edit and update these details anytime in the **Profile** tab with Room database persistence.",
                    hindiText = "👤 **आपका सहेजा गया किसान प्रोफाइल:**\n• **नाम:** ${farmer.fullName}\n• **पंजीकरण संख्या:** ${farmer.registrationId}\n• **फोन:** ${farmer.phone}\n• **पसंदीदा मंडी:** ${farmer.preferredCentreName}\n• **कृषि भूमि:** ${farmer.landAreaAcres} एकड़ (${farmer.village}, ${farmer.district})\n• **मुख्य फसल:** ${farmer.primaryCrop.hindiName} (~${farmer.expectedQuantityQuintals} क्विंटल)\n\nआप **प्रोफाइल** टैब में जाकर रूम डेटाबेस में यह जानकारी कभी भी अपडेट कर सकते हैं।",
                    category = "GENERAL",
                    suggestedQuestions = listOf("How to book slot?", "What is current Wheat MSP?"),
                    hindiSuggestedQuestions = listOf("स्लॉट कैसे बुक करें?", "गेहूं का एमएसपी क्या है?")
                )
            }

            // 8. Default Greeting / General Assistance
            else -> {
                BotResponse(
                    englishText = "Namaste Kisan Bhai! 🙏 I am **Krishi Sahayak**, your 24/7 digital procurement assistant.\n\nI can help you with:\n• **MSP Rates & Crop Prices** (Wheat, Mustard, Paddy)\n• **Mandi Slot Booking & Token QR Pass**\n• **Live Queue Position & Wait Times**\n• **Moisture & Quality Testing Norms**\n• **Direct Benefit Transfer (DBT) Bank Payment Status**\n• **Gate Pass Rules & Required Documents**\n\nTap the microphone 🎙️ button to speak in Hindi or English, or tap any question below!",
                    hindiText = "नमस्ते किसान भाई! 🙏 मैं आपका **कृषि सहायक** हूँ।\n\nमैं आपकी इन विषयों में सहायता कर सकता हूँ:\n• **सरकारी न्यूनतम समर्थन मूल्य (MSP)** (गेहूं, सरसों, धान आदि)\n• **मंडी स्लॉट बुकिंग और डिजिटल टोकन**\n• **लाइव कतार में आपका नंबर व प्रतीक्षा समय**\n• **फसल में नमी व गुणवत्ता के सरकारी नियम**\n• **डीबीटी बैंक भुगतान स्थिति (48-72 घंटे)**\n• **मंडी गेट पास और जरूरी कागजात**\n\nमाइक 🎙️ बटन दबाकर हिंदी में बोलें या नीचे दिए गए प्रश्नों में से चुनें!",
                    category = "GENERAL",
                    suggestedQuestions = listOf("What is Wheat MSP rate?", "How to book a slot?", "When will I get DBT payment?", "What is the moisture limit?"),
                    hindiSuggestedQuestions = listOf("गेहूं का सरकारी भाव क्या है?", "स्लॉट कैसे बुक करें?", "खाते में पैसा कब आएगा?", "फसल में नमी कितनी होनी चाहिए?")
                )
            }
        }
    }
}
