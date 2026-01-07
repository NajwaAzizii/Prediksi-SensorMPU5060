package com.example.najwa_belajarnavigationdrawer

data class BlogPost(
    var id: String? = "",
    var title: String? = "",
    var content: String? = "",
    var author: String? = "",
    var thumbnailUrl: String? = "",
    var thumbnailBase64: String? = "",   // ✅ TAMBAHAN
    var createdAt: Long? = 0L
)
