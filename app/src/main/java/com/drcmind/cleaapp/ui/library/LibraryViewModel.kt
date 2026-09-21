package com.drcmind.cleaapp.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drcmind.cleaapp.domain.model.Article
import com.drcmind.cleaapp.domain.model.ArticleCategory
import com.drcmind.cleaapp.domain.repository.ArticleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LibraryUiState(
    val isLoading: Boolean = false,
    val articles: List<Article> = emptyList(),
    val selectedCategory: ArticleCategory? = null,
    val showFavoritesOnly: Boolean = false,
    val searchQuery: String = "",
    val selectedArticle: Article? = null,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<ArticleCategory?>(null)
    private val _showFavoritesOnly = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _selectedArticle = MutableStateFlow<Article?>(null)
    private val _error = MutableStateFlow<String?>(null)

    private val _articlesFlow = combine(_selectedCategory, _showFavoritesOnly) { cat, favs ->
        cat to favs
    }.flatMapLatest { (cat, favs) ->
        articleRepository.getLocalArticles(category = cat, favoritesOnly = favs)
    }

    val state: StateFlow<LibraryUiState> = combine(
        _articlesFlow,
        _selectedCategory,
        _showFavoritesOnly,
        _searchQuery,
        _selectedArticle,
        _isLoading,
        _error
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val allArticles = args[0] as List<Article>
        val category = args[1] as? ArticleCategory
        val favs = args[2] as Boolean
        val query = args[3] as String
        val selectedArt = args[4] as? Article
        val loading = args[5] as Boolean
        val err = args[6] as? String

        val filtered = if (query.isBlank()) {
            allArticles
        } else {
            allArticles.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.summary.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true)
            }
        }

        LibraryUiState(
            isLoading = loading,
            articles = filtered,
            selectedCategory = category,
            showFavoritesOnly = favs,
            searchQuery = query,
            selectedArticle = selectedArt,
            error = err
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryUiState(isLoading = true))

    init {
        refresh()
    }

    fun setCategory(category: ArticleCategory?) {
        _showFavoritesOnly.value = false
        _selectedCategory.value = category
    }

    fun setShowFavoritesOnly(show: Boolean) {
        _selectedCategory.value = null
        _showFavoritesOnly.value = show
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectArticle(article: Article?) {
        _selectedArticle.value = article
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = articleRepository.refreshArticles(_selectedCategory.value, _searchQuery.value.ifBlank { null })
            _isLoading.value = false
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun toggleFavorite(article: Article) {
        viewModelScope.launch {
            articleRepository.toggleFavorite(article.id, article.isFavorite)
            // Mise à jour de l'article sélectionné si ouvert
            if (_selectedArticle.value?.id == article.id) {
                _selectedArticle.value = _selectedArticle.value?.copy(isFavorite = !article.isFavorite)
            }
        }
    }
}
