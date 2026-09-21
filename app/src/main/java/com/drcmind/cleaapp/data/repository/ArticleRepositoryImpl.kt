package com.drcmind.cleaapp.data.repository

import com.drcmind.cleaapp.data.local.room.dao.ArticleDao
import com.drcmind.cleaapp.data.local.room.entity.ArticleEntity
import com.drcmind.cleaapp.data.mapper.toDomain
import com.drcmind.cleaapp.data.mapper.toEntity
import com.drcmind.cleaapp.data.remote.api.ArticleApiService
import com.drcmind.cleaapp.data.remote.dto.CategoryDto
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
            dao.getArticlesByCategory(category.slug).map { list -> list.map { it.toDomain() } }
        }
    }

    override suspend fun refreshArticles(
        category: ArticleCategory?,
        search: String?,
        favoritesOnly: Boolean
    ): Result<List<Article>> = try {
        val dtos = try {
            api.getArticles(
                category = category?.slug,
                q = search,
                favoritesOnly = favoritesOnly
            )
        } catch (e: Exception) {
            emptyList()
        }

        if (dtos.isNotEmpty()) {
            dao.insertAll(dtos.map { it.toEntity() })
            Result.success(dtos.map { it.toDomain() })
        } else {
            // Seed local Room cache if database is empty
            val count = dao.getArticleById("art_1")
            if (count == null) {
                dao.insertAll(curatedArticles)
            }
            val localList = when {
                favoritesOnly -> curatedArticles.filter { it.isFavorite }
                category != null -> curatedArticles.filter { it.category == category.slug }
                else -> curatedArticles
            }
            Result.success(localList.map { it.toDomain() })
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCategories(): Result<List<CategoryDto>> = try {
        val categories = api.getCategories()
        if (categories.isNotEmpty()) {
            Result.success(categories)
        } else {
            Result.success(defaultCategories)
        }
    } catch (e: Exception) {
        Result.success(defaultCategories)
    }

    override suspend fun toggleFavorite(slugOrId: String, currentFavorite: Boolean): Result<Unit> = try {
        val newFav = !currentFavorite
        dao.updateFavorite(slugOrId, newFav)
        try {
            if (newFav) {
                api.addFavorite(slugOrId)
            } else {
                api.removeFavorite(slugOrId)
            }
        } catch (_: Exception) {}
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getArticleDetail(slugOrId: String): Result<Article> = try {
        val local = dao.getArticleById(slugOrId)?.toDomain() ?: dao.getArticleBySlug(slugOrId)?.toDomain()
        if (local != null && local.content.length > 200) {
            Result.success(local)
        } else {
            val remote = api.getArticleDetail(slugOrId).toDomain()
            dao.insertAll(listOf(remote.let { 
                ArticleEntity(
                    id = it.id,
                    title = it.title,
                    slug = it.slug,
                    category = it.category.slug,
                    categoryName = it.categoryName,
                    summary = it.summary,
                    content = it.content,
                    readingTimeMinutes = it.readingTimeMinutes,
                    viewsCount = it.viewsCount,
                    authorName = it.authorName,
                    imageUrl = it.imageUrl,
                    isFavorite = it.isFavorite,
                    publishedAt = it.publishedAt
                )
            }))
            Result.success(remote)
        }
    } catch (e: Exception) {
        val local = dao.getArticleById(slugOrId)?.toDomain() ?: dao.getArticleBySlug(slugOrId)?.toDomain()
        if (local != null) Result.success(local) else Result.failure(e)
    }

    companion object {
        val defaultCategories = listOf(
            CategoryDto(id = "cat_1", name = "Santé menstruelle", slug = "sante-menstruelle", icon = "health", position = 1),
            CategoryDto(id = "cat_2", name = "Planning familial", slug = "planning-familial", icon = "family", position = 2),
            CategoryDto(id = "cat_3", name = "Couple", slug = "couple", icon = "heart", position = 3),
            CategoryDto(id = "cat_4", name = "Leadership", slug = "leadership", icon = "star", position = 4),
            CategoryDto(id = "cat_5", name = "Spiritualité", slug = "spiritualite", icon = "spa", position = 5),
            CategoryDto(id = "cat_6", name = "Développement personnel", slug = "developpement-personnel", icon = "growth", position = 6)
        )

        val curatedArticles = listOf(
            ArticleEntity(
                id = "art_1",
                title = "Alimentation et équilibre hormonal : les clés de chaque phase",
                slug = "alimentation-et-equilibre-hormonal",
                category = "sante-menstruelle",
                categoryName = "Santé menstruelle",
                summary = "Découvrez comment adapter vos apports nutritionnels selon vos 4 phases hormonales pour réduire la fatigue et les crampes.",
                content = """
                    # Alimentation et équilibre hormonal : les clés de chaque phase
                    
                    Comprendre les variations hormonales tout au long de votre cycle menstruel est un formidable levier pour retrouver vitalité et sérénité.
                    
                    ---
                    
                    ## 🌿 1. Phase Folliculaire (Jours 6 à 13)
                    Les œstrogènes augmentent. Votre métabolisme est plus dynamique :
                    - Privilégiez les **légumes verts crus** et graines germées.
                    - Intégrez des **protéines maigres** et des acides gras essentiels (graines de lin et de courge).
                    
                    ## ☀️ 2. Phase Ovulatoire (Jours 14 à 16)
                    Pic d'énergie et d'œstrogènes :
                    - Favorisez les **aliments antioxydants** et fruits rouges.
                    - Maintenez une **excellente hydratation** pour soutenir le foie dans l'élimination des métabolites.
                    
                    ## 🍂 3. Phase Lutéale (Jours 17 à 28)
                    La progestérone domine :
                    - Augmentez vos apports en **magnésium** (chocolat noir, amandes, épinards).
                    - Privilégiez les **glucides complexes** (patates douces, riz complet) pour stabiliser la sérotonine.
                    
                    ## 🌙 4. Phase Menstruelle (Jours 1 à 5)
                    Vos taux d'hormones sont au plus bas :
                    - Chouchoutez votre organisme avec des **plats chauds et réconfortants**.
                    - Misez sur le **fer biodisponible** (lentilles, spiruline) et des infusions de camomille ou gingembre.
                """.trimIndent(),
                readingTimeMinutes = 4,
                viewsCount = 142,
                authorName = "Dr. Claire N., Gynécologue & Nutritionniste",
                imageUrl = null,
                isFavorite = true,
                publishedAt = "2026-03-15"
            ),
            ArticleEntity(
                id = "art_2",
                title = "Gérer son énergie professionnelle et son leadership au féminin",
                slug = "gerer-son-energie-professionnelle-leadership",
                category = "leadership",
                categoryName = "Leadership",
                summary = "Optimisez vos prises de parole, vos négociations et vos sessions stratégiques en accord avec vos cycles naturels.",
                content = """
                    # Gérer son énergie professionnelle et son leadership au féminin
                    
                    Le leadership durable repose sur l'écoute de ses rythmes biologiques plutôt que sur une intensité constante qui mène à l'épuisement.
                    
                    ---
                    
                    ## ✨ Aligner son agenda sur ses cycles
                    
                    - **Semaine créative (Phase folliculaire)** : Idéale pour lancer des projets, brainstormer et concevoir de nouvelles stratégies.
                    - **Semaine d'affirmation (Phase ovulatoire)** : Maximisez vos prises de parole en public, entretiens décisifs et réunions de négociation. Votre communication est naturelle et captivante.
                    - **Semaine d'analyse et de clôture (Phase lutéale)** : Excellente période pour la rigueur, l'audit, la relecture de dossiers et le tri des priorités.
                    - **Semaine de recul (Phase menstruelle)** : Prenez de la hauteur, évitez la surcharge de réunions et privilégiez la vision long terme.
                """.trimIndent(),
                readingTimeMinutes = 5,
                viewsCount = 98,
                authorName = "Aline M., Coach en Leadership & Management",
                imageUrl = null,
                isFavorite = false,
                publishedAt = "2026-03-10"
            ),
            ArticleEntity(
                id = "art_3",
                title = "La puissance du temps pour soi : rituel du soir et sérénité",
                slug = "la-puissance-du-temps-pour-soi",
                category = "spiritualite",
                categoryName = "Spiritualité",
                summary = "5 minutes de gratitude et de reconnexion intérieure pour apaiser l'esprit avant le sommeil.",
                content = """
                    # La puissance du temps pour soi : rituel du soir
                    
                    Dans un quotidien où les responsabilités familiales et professionnelles s'enchaînent, s'accorder un espace de sanctuaire est essentiel.
                    
                    ---
                    
                    ## 🌸 Le Rituel des 3 Gratitudes
                    
                    Avant d'éteindre la lumière :
                    1. **Remerciez pour une victoire concrète** accomplie aujourd'hui, aussi modeste soit-elle.
                    2. **Notez un moment de bienveillance** partagé avec un proche ou un collègue.
                    3. **Respirez profondément pendant 60 secondes** en visualisant le relâchement de vos épaules et de votre nuque.
                    
                    > Ce rituel stimule le système nerveux parasympathique et prépare un sommeil réparateur profond.
                """.trimIndent(),
                readingTimeMinutes = 3,
                viewsCount = 210,
                authorName = "Cléa Bien-être",
                imageUrl = null,
                isFavorite = true,
                publishedAt = "2026-03-01"
            ),
            ArticleEntity(
                id = "art_4",
                title = "Communiquer avec son partenaire sans tabou sur son cycle",
                slug = "communiquer-avec-son-partenaire-sans-tabou",
                category = "couple",
                categoryName = "Couple",
                summary = "Comment partager ses besoins et ses ressentis avec son conjoint pour renforcer la complicité et la compréhension mutuelle.",
                content = """
                    # Communiquer avec son partenaire sans tabou sur son cycle
                    
                    La transparence au sein du couple désamorce les incompréhensions liées aux variations d'humeur ou de désir.
                    
                    ---
                    
                    ## ❤️ 3 clés pour une discussion sereine
                    
                    1. **Expliquer la physiologie simplement** : partagez que la fatigue pré-menstruelle n'est pas une saute d'humeur mais une variation hormonale réelle.
                    2. **Exprimer des demandes claires** : *« Cette semaine, j'ai besoin d'un peu plus de temps de repos le soir. Pourrais-tu t'occuper du dîner mardi ? »*
                    3. **Impliquer l'autre dans la planification familiale** et l'harmonie du foyer.
                """.trimIndent(),
                readingTimeMinutes = 4,
                viewsCount = 175,
                authorName = "Sarah K., Conseillère conjugale",
                imageUrl = null,
                isFavorite = false,
                publishedAt = "2026-02-25"
            ),
            ArticleEntity(
                id = "art_5",
                title = "Comprendre sa fertilité et la méthode d'observation du cycle",
                slug = "comprendre-sa-fertilite-observation-du-cycle",
                category = "planning-familial",
                categoryName = "Planning familial",
                summary = "Les repères naturels (température, glaire cervicale) pour identifier sa fenêtre de fertilité avec précision.",
                content = """
                    # Comprendre sa fertilité et la méthode d'observation
                    
                    L'observation de ses biomarqueurs offre une compréhension intime et autonome de sa fertilité.
                    
                    ---
                    
                    ## 🔍 Les indicateurs clés
                    - **La glaire cervicale** : Devient fluide, transparente et étirable à l'approche de l'ovulation.
                    - **La température basale** : S'élève de 0.2°C à 0.5°C juste après l'ovulation sous l'effet de la progestérone.
                    - **Le col de l'utérus** : Devient plus haut, plus mou et plus ouvert en période fertile.
                """.trimIndent(),
                readingTimeMinutes = 6,
                viewsCount = 89,
                authorName = "Dr. Valérie M., Sage-femme",
                imageUrl = null,
                isFavorite = false,
                publishedAt = "2026-02-20"
            ),
            ArticleEntity(
                id = "art_6",
                title = "Développer son estime personnelle et poser des limites saines",
                slug = "developper-son-estime-personnelle-limites",
                category = "developpement-personnel",
                categoryName = "Développement personnel",
                summary = "Apprenez à dire non avec bienveillance et à valoriser votre temps et votre espace mental.",
                content = """
                    # Développer son estime personnelle et poser des limites saines
                    
                    Savoir poser des limites claires est un acte d'amour propre et de respect mutuel.
                    
                    ---
                    
                    ## 🛡️ L'art du « Non » constructif
                    - **Clarifier son intention** : Dire non à une sollicitation non prioritaire, c'est dire oui à son équilibre.
                    - **Formuler sans justification excessive** : *« Merci d'avoir pensé à moi, mais je ne serai pas en mesure de m'en charger cette semaine. »*
                    - **Protéger son énergie** : Accordez-vous des plages horaires sans notification.
                """.trimIndent(),
                readingTimeMinutes = 4,
                viewsCount = 130,
                authorName = "Estelle R., Psychologue clinicienne",
                imageUrl = null,
                isFavorite = false,
                publishedAt = "2026-02-15"
            )
        )
    }
}
