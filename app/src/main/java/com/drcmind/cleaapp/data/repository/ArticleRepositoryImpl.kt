package com.drcmind.cleaapp.data.repository

import com.drcmind.cleaapp.data.local.room.dao.ArticleDao
import com.drcmind.cleaapp.data.local.room.entity.ArticleEntity
import com.drcmind.cleaapp.data.mapper.toDomain
import com.drcmind.cleaapp.data.mapper.toEntity
import com.drcmind.cleaapp.data.remote.api.ArticleApiService
import com.drcmind.cleaapp.domain.model.Article
import com.drcmind.cleaapp.domain.model.ArticleCategory
import com.drcmind.cleaapp.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ArticleRepositoryImpl(
    private val api: ArticleApiService,
    private val dao: ArticleDao
) : ArticleRepository {

    override fun getLocalArticles(
        category: ArticleCategory?,
        favoritesOnly: Boolean
    ): Flow<List<Article>> {
        return if (favoritesOnly) {
            dao.getFavoriteArticles().map { list -> list.map { it.toDomain() } }
        } else if (category == null) {
            dao.getAllArticles().map { list -> list.map { it.toDomain() } }
        } else {
            dao.getArticlesByCategory(category.apiValue).map { list -> list.map { it.toDomain() } }
        }
    }

    override suspend fun refreshArticles(
        category: ArticleCategory?,
        search: String?
    ): Result<List<Article>> = try {
        val dtos = try {
            api.getArticles(category = category?.apiValue, search = search)
        } catch (e: Exception) {
            emptyList()
        }

        if (dtos.isNotEmpty()) {
            dao.insertAll(dtos.map { it.toEntity() })
            Result.success(dtos.map { it.toDomain() })
        } else {
            // Si le serveur n'a pas encore d'articles ou est hors-ligne, nous insérons le catalogue de référence CLEA
            val count = dao.getArticleById("art_1")
            if (count == null) {
                dao.insertAll(curatedArticles)
            }
            Result.success(curatedArticles.map { it.toDomain() })
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun toggleFavorite(articleId: String, currentFavorite: Boolean): Result<Unit> = try {
        val newFav = !currentFavorite
        dao.updateFavorite(articleId, newFav)
        try {
            api.toggleFavorite(articleId)
        } catch (_: Exception) {}
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getArticleDetail(articleId: String): Result<Article> = try {
        val local = dao.getArticleById(articleId)?.toDomain()
        if (local != null) {
            Result.success(local)
        } else {
            val remote = api.getArticleDetail(articleId).toDomain()
            Result.success(remote)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    companion object {
        val curatedArticles = listOf(
            ArticleEntity(
                id = "art_1",
                title = "Alimentation et équilibre hormonal : les clés de chaque phase",
                category = "menstruation",
                summary = "Découvrez comment adapter vos apports nutritionnels selon vos 4 phases hormonales pour réduire la fatigue et les crampes.",
                content = """
                    Comprendre les variations hormonales tout au long de votre cycle menstruel est un formidable levier pour retrouver vitalité et sérénité.
                    
                    🌿 1. Phase Folliculaire (Jours 6 à 13)
                    Les oestrogènes augmentent. Votre métabolisme est plus dynamique : privilégiez les légumes verts crus, les graines germées, les protéines maigres et les acides gras essentiels (graines de lin et de courge).
                    
                    ☀️ 2. Phase Ovulatoire (Jours 14 à 16)
                    Pic d'énergie et d'oestrogènes. Privilégiez les aliments antioxydants, les fruits rouges et maintenez une excellente hydratation pour soutenir le foie dans l'élimination des métabolites.
                    
                    🍂 3. Phase Lutéale (Jours 17 à 28)
                    La progestérone domine. C'est le moment d'augmenter les apports en magnésium (chocolat noir, amandes, épinards) et en glucides complexes (patates douces, riz complet) pour stabiliser la sérotonine et éviter les fringales.
                    
                    🌙 4. Phase Menstruelle (Jours 1 à 5)
                    Vos taux d'hormones sont au plus bas. Chouchoutez votre organisme avec des plats chauds et réconfortants, riches en fer biodisponible (lentilles, viandes maigres ou spiruline) et des infusions de camomille ou gingembre.
                """.trimIndent(),
                readingTimeMinutes = 4,
                authorName = "Dr. Claire N., Gynécologue & Nutritionniste",
                imageUrl = null,
                isFavorite = true,
                publishedAt = "2026-03-15"
            ),
            ArticleEntity(
                id = "art_2",
                title = "Gérer son énergie professionnelle et son leadership au féminin",
                category = "leadership",
                summary = "Optimisez vos prises de parole, vos négociations et vos sessions stratégiques en accord avec vos cycles naturels.",
                content = """
                    Le leadership durable repose sur l'écoute de ses rythmes biologiques plutôt que sur une intensité constante qui mène à l'épuisement.
                    
                    ✨ Aligner son agenda sur ses cycles
                    • Semaine créative (Phase folliculaire) : Idéale pour lancer des projets, brainstormer et concevoir de nouvelles stratégies.
                    • Semaine d'affirmation (Phase ovulatoire) : Maximisez vos prises de parole en public, entretiens décisifs et réunions de négociation. Votre communication est naturelle et captivante.
                    • Semaine d'analyse et de clôture (Phase lutéale) : Excellente période pour la rigueur, l'audit, la relecture de dossiers et le tri des priorités.
                    • Semaine de recul (Phase menstruelle) : Prenez de la hauteur, évitez la surcharge de réunions et privilégiez la vision long terme.
                """.trimIndent(),
                readingTimeMinutes = 5,
                authorName = "Aline M., Coach en Leadership & Management",
                imageUrl = null,
                isFavorite = false,
                publishedAt = "2026-03-10"
            ),
            ArticleEntity(
                id = "art_3",
                title = "La puissance du temps pour soi : rituel du soir et sérénité",
                category = "spirituality",
                summary = "5 minutes de gratitude et de reconnexion intérieure pour apaiser l'esprit avant le sommeil.",
                content = """
                    Dans un quotidien où les responsabilités familiales et professionnelles s'enchaînent, s'accorder un espace de sanctuaire est essentiel.
                    
                    🌸 Le Rituel des 3 Gratitudes
                    Avant d'éteindre la lumière :
                    1. Remerciez pour une victoire concrète accomplie aujourd'hui, aussi modeste soit-elle.
                    2. Notez un moment de bienveillance partagé avec un proche.
                    3. Respirez profondément pendant 60 secondes en visualisant le relâchement de vos épaules et de votre nuque.
                    
                    Ce rituel stimule le système nerveux parasympathique et prépare un sommeil réparateur profond.
                """.trimIndent(),
                readingTimeMinutes = 3,
                authorName = "Cléa Bien-être",
                imageUrl = null,
                isFavorite = true,
                publishedAt = "2026-03-01"
            ),
            ArticleEntity(
                id = "art_4",
                title = "Communiquer avec son partenaire sans tabou sur son cycle",
                category = "couple",
                summary = "Comment partager ses besoins et ses ressentis avec son conjoint pour renforcer la complicité et la compréhension mutuelle.",
                content = """
                    La transparence au sein du couple désamorce les incompréhensions liées aux variations d'humeur ou de désir.
                    
                    ❤️ 3 clés pour une discussion sereine :
                    1. Expliquer la physiologie simplement : partagez que la fatigue pré-menstruelle n'est pas une saute d'humeur mais une variation hormonale réelle.
                    2. Exprimer des demandes claires : « Cette semaine, j'ai besoin d'un peu plus de temps de repos le soir. Pourrais-tu t'occuper du dîner mardi ? »
                    3. Impliquer l'autre dans la planification familiale et l'harmonie du foyer.
                """.trimIndent(),
                readingTimeMinutes = 4,
                authorName = "Sarah K., Conseillère conjugale",
                imageUrl = null,
                isFavorite = false,
                publishedAt = "2026-02-25"
            )
        )
    }
}
