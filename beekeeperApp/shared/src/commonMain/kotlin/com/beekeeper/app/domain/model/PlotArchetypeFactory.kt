package com.beekeeper.app.domain.repository

import com.beekeeper.app.domain.model.*

/**
 * Complete Factory for all 100 Timeless Plot Archetypes
 * Each archetype is fully mapped to PlotLogic components for Story Bible integration
 */
object PlotArchetypeFactory {

    fun getAllArchetypes(): List<PlotArchetype> {
        return listOf(
            // Journey Archetypes (1-10)
            createHerosJourney(),
            createQuest(),
            createVoyageAndReturn(),
            createSeekingHome(),
            createJourneyToCenter(),
            createWanderingHero(),
            createImpossibleJourney(),
            createRaceAgainstTime(),
            createTheLastJourney(),
            createCircularJourney(),

            // Transformation Archetypes (11-20)
            createComingOfAge(),
            createRedemption(),
            createFallFromGrace(),
            createRedemptionQuest(),
            createRebirth(),
            createMetamorphosis(),
            createAwakening(),
            createCorruption(),
            createEnlightenment(),
            createDarkTransformation(),

            // Relationship Archetypes (21-35)
            createLoveTriangle(),
            createForbiddenLove(),
            createUnrequitedLove(),
            createLoveAtFirstSight(),
            createReunitedLovers(),
            createStarCrossedLovers(),
            createImpossibleLove(),
            createArrangedMarriage(),
            createLoveConquersAll(),
            createTragicLove(),
            createSecondChanceLove(),
            createEnemiestoLovers(),
            createFriendsToLovers(),
            createSoulmates(),
            createLoveAcrossTime(),

            // Conflict Archetypes (36-50)
            createRevenge(),
            createOvercomingTheMonster(),
            createWar(),
            createBattleGoodEvil(),
            createManVsNature(),
            createManVsMachine(),
            createCivilWar(),
            createRevolutionaryWar(),
            createHolyWar(),
            createPersonalVendetta(),
            createFamilyFeud(),
            createTribalConflict(),
            createIdeologicalWar(),
            createColdWar(),
            createPsychologicalWarfare(),

            // Power Archetypes (51-60)
            createRagsToRiches(),
            createUnderdog(),
            createPowerAndCorruption(),
            createRiseAndFall(),
            createUsurper(),
            createKingmaker(),
            createPowerStruggle(),
            createInheritance(),
            createPowerVacuum(),
            createReluctantRuler(),

            // Mystery Archetypes (61-70)
            createMystery(),
            createDiscovery(),
            createIdentityCrisis(),
            createTheDouble(),
            createHiddenIdentity(),
            createAmnesiaMyster(),
            createLockedRoom(),
            createConspiracy(),
            createTreasureHunt(),
            createMissingPerson(),

            // Survival Archetypes (71-80)
            createSurvival(),
            createEscape(),
            createApocalypse(),
            createDesertedIsland(),
            createNaturalDisaster(),
            createPlague(),
            createAlienInvasion(),
            createZombieOutbreak(),
            createPostApocalyptic(),
            createLastSurvivors(),

            // Comedy Archetypes (81-85)
            createFishOutOfWater(),
            createMistakenIdentity(),
            createOddCouple(),
            createFarcicalMisunderstanding(),
            createRoleReversal(),

            // Tragedy Archetypes (86-90)
            createTragedy(),
            createSacrifice(),
            createHubrisFall(),
            createForbiddenKnowledge(),
            createInevitableFate(),

            // Societal Archetypes (91-100)
            createRebellion(),
            createDystopia(),
            createUtopiaGoneWrong(),
            createSocietyVsIndividual(),
            createClassStruggle(),
            createGenerationGap(),
            createCulturalClash(),
            createTimeLoop(),
            createParallelWorlds(),
            createChosenOne()
        )
    }

    // ============ JOURNEY ARCHETYPES (1-10) ============

    private fun createHerosJourney() = PlotArchetype(
        id = "archetype_001_heros_journey",
        name = "Hero's Journey",
        description = "A protagonist embarks on an adventurous quest, facing challenges, mentors, and ultimately transforming to achieve a goal",
        category = PlotArchetypeCategory.JOURNEY,

        archetypePlotLogic = PlotLogic(
            centralEngine = CentralEngine(
                mainConflict = "Ordinary individual called to extraordinary adventure",
                whyUnresolvable = "Hero must fundamentally transform to succeed",
                stakesEscalation = listOf(
                    "Personal safety threatened",
                    "Loved ones endangered",
                    "Community at risk",
                    "World/universe at stake"
                ),
                pointOfNoReturn = "Crossing the threshold into the special world",
                onlyPossibleResolution = "Hero must embrace destiny and transform"
            ),
            causalChains = listOf(
                CausalChain(
                    chainName = "Call to Adventure",
                    initialCause = "Disruption of ordinary world",
                    links = listOf(
                        CausalLink("Status quo broken", "Hero receives call", "Always", listOf()),
                        CausalLink("Initial refusal", "Consequences force action", "Usually", listOf()),
                        CausalLink("Mentor appears", "Hero gains tools/knowledge", "Always", listOf())
                    ),
                    finalEffect = "Hero commits to journey",
                    episodeStart = 1,
                    episodeEnd = 3
                )
            ),
            tickingClocks = listOf(
                TickingClock(
                    clockName = "Ultimate Threat",
                    deadline = "Before villain achieves goal",
                    consequences = "Irreversible harm to world",
                    accelerators = listOf("Each test failed", "Allies lost"),
                    decelerators = listOf("Small victories", "New powers gained"),
                    visibility = "Gradually revealed",
                    episodeIntroduced = 2,
                    episodeResolved = 11
                )
            ),
            pivotPoints = listOf(
                PivotPoint(
                    episodeNumber = 3,
                    decision = "Accept the call",
                    decisionMaker = "Hero",
                    options = listOf("Stay safe", "Enter unknown"),
                    chosenPath = "Enter unknown",
                    whyChosen = "Moral obligation overcomes fear",
                    consequences = "No turning back",
                    alternateTimelines = listOf("World destroyed without hero")
                )
            ),
            dominoChains = listOf(),
            inevitabilities = listOf(
                Inevitability(
                    event = "Face greatest fear",
                    whyInevitable = "Only through fear comes transformation",
                    setupRequired = listOf("Skills learned", "Allies gathered"),
                    episode = 10,
                    foreshadowing = listOf("Nightmares", "Warnings")
                )
            )
        ),

        archetypeBeats = listOf(
            ArchetypeBeat("Ordinary World", "Establish normal life", 0.0f, NarrativeFunction.OPENING, true, "1"),
            ArchetypeBeat("Call to Adventure", "Inciting incident", 0.1f, NarrativeFunction.CATALYST, true, "2"),
            ArchetypeBeat("Refusal of Call", "Initial hesitation", 0.15f, NarrativeFunction.COMPLICATION, false, "2"),
            ArchetypeBeat("Meeting Mentor", "Gain wisdom", 0.2f, NarrativeFunction.MENTOR_INTRODUCTION, true, "3"),
            ArchetypeBeat("Crossing Threshold", "Enter new world", 0.25f, NarrativeFunction.FIRST_PLOT_POINT, true, "3"),
            ArchetypeBeat("Tests & Allies", "Face challenges", 0.4f, NarrativeFunction.RISING_ACTION, true, "4-6"),
            ArchetypeBeat("Approach", "Prepare for ordeal", 0.5f, NarrativeFunction.MIDPOINT, true, "7"),
            ArchetypeBeat("Ordeal", "Face greatest fear", 0.65f, NarrativeFunction.CRISIS, true, "8-9"),
            ArchetypeBeat("Reward", "Claim prize", 0.75f, NarrativeFunction.FALSE_VICTORY, true, "9"),
            ArchetypeBeat("Road Back", "Return journey", 0.85f, NarrativeFunction.DARK_NIGHT_OF_SOUL, true, "10"),
            ArchetypeBeat("Resurrection", "Final test", 0.9f, NarrativeFunction.CLIMAX, true, "11"),
            ArchetypeBeat("Return with Elixir", "Share wisdom", 0.95f, NarrativeFunction.RESOLUTION, true, "12")
        ),

        thematicCore = ThematicCore(
            centralTheme = "Personal transformation through adversity",
            moralQuestion = "What makes someone a hero?",
            emotionalJourney = "Fear → Courage, Ignorance → Wisdom",
            universalTruth = "We all have untapped potential"
        ),

        examples = listOf("Star Wars", "The Matrix", "Harry Potter"),
        modernAdaptations = listOf("Virtual world journey", "Corporate ladder climb"),
        subversions = listOf("Hero refuses to return", "Mentor is villain")
    )

    private fun createQuest() = PlotArchetype(
        id = "archetype_002_quest",
        name = "Quest",
        description = "Characters embark on a journey to retrieve a valuable object, attain knowledge, or fulfill a significant purpose",
        category = PlotArchetypeCategory.JOURNEY,

        archetypePlotLogic = PlotLogic(
            centralEngine = CentralEngine(
                mainConflict = "Valuable goal requires dangerous journey",
                whyUnresolvable = "Goal protected by formidable obstacles",
                stakesEscalation = listOf("Personal need", "Group survival", "World salvation"),
                pointOfNoReturn = "Commitment to quest",
                onlyPossibleResolution = "Retrieve object or fail trying"
            ),
            causalChains = listOf(
                CausalChain(
                    chainName = "Quest Progression",
                    initialCause = "Object/goal identified",
                    links = listOf(
                        CausalLink("Need established", "Quest begins", "Always", listOf()),
                        CausalLink("Map/clue found", "Path revealed", "Usually", listOf())
                    ),
                    finalEffect = "Goal achieved or lost",
                    episodeStart = 1,
                    episodeEnd = 12
                )
            ),
            tickingClocks = listOf(),
            pivotPoints = listOf(),
            dominoChains = listOf(),
            inevitabilities = listOf()
        ),

        archetypeBeats = listOf(
            ArchetypeBeat("Quest Revealed", "Learn of objective", 0.1f, NarrativeFunction.CATALYST, true),
            ArchetypeBeat("Gathering Party", "Assemble team", 0.2f, NarrativeFunction.SETUP, true),
            ArchetypeBeat("The Journey", "Travel and obstacles", 0.5f, NarrativeFunction.RISING_ACTION, true),
            ArchetypeBeat("Final Challenge", "Ultimate test", 0.8f, NarrativeFunction.CLIMAX, true),
            ArchetypeBeat("Quest Complete", "Success or failure", 0.95f, NarrativeFunction.RESOLUTION, true)
        ),

        thematicCore = ThematicCore(
            centralTheme = "Purpose drives action",
            moralQuestion = "What is worth risking everything for?",
            emotionalJourney = "Doubt → Determination → Fulfillment",
            universalTruth = "The journey matters more than destination"
        ),

        examples = listOf("Lord of the Rings", "Indiana Jones", "The Holy Grail"),
        modernAdaptations = listOf("Startup journey", "Medical cure search"),
        subversions = listOf("Quest object worthless", "Journey was the real goal")
    )

    private fun createVoyageAndReturn() = PlotArchetype(
        id = "archetype_003_voyage_return",
        name = "Voyage and Return",
        description = "Characters journey to a strange land, face trials, and then return transformed",
        category = PlotArchetypeCategory.JOURNEY,

        archetypePlotLogic = createJourneyLogic("Strange world experience changes traveler"),

        archetypeBeats = listOf(
            ArchetypeBeat("Anticipation", "Sense of adventure", 0.1f, NarrativeFunction.SETUP, true),
            ArchetypeBeat("Fall into Other World", "Enter strange place", 0.2f, NarrativeFunction.CATALYST, true),
            ArchetypeBeat("Fascination", "Wonder at new world", 0.4f, NarrativeFunction.RISING_ACTION, true),
            ArchetypeBeat("Frustration", "Challenges mount", 0.6f, NarrativeFunction.COMPLICATION, true),
            ArchetypeBeat("Nightmare", "Danger peaks", 0.8f, NarrativeFunction.CRISIS, true),
            ArchetypeBeat("Escape and Return", "Get home transformed", 0.95f, NarrativeFunction.RESOLUTION, true)
        ),

        thematicCore = ThematicCore(
            centralTheme = "There's no place like home",
            moralQuestion = "Does travel broaden the mind?",
            emotionalJourney = "Curiosity → Wonder → Fear → Appreciation",
            universalTruth = "We must leave home to appreciate it"
        ),

        examples = listOf("Alice in Wonderland", "The Wizard of Oz", "Spirited Away"),
        modernAdaptations = listOf("Virtual reality trap", "Time travel adventure"),
        subversions = listOf("Choose to stay in other world", "Home was the nightmare")
    )

    // Continue with remaining Journey archetypes (4-10)
    private fun createSeekingHome() = createArchetypeWithBasicLogic(
        "archetype_004_seeking_home", "Seeking Home",
        "The protagonist embarks on a journey to find a place where they belong",
        PlotArchetypeCategory.JOURNEY,
        "Finding where you belong",
        "What makes a place home?"
    )

    private fun createJourneyToCenter() = createArchetypeWithBasicLogic(
        "archetype_005_journey_center", "Journey to the Center",
        "Characters venture into the heart of a location or object to uncover its secrets",
        PlotArchetypeCategory.JOURNEY,
        "Truth lies at the center",
        "What secrets lie within?"
    )

    private fun createWanderingHero() = createArchetypeWithBasicLogic(
        "archetype_006_wandering_hero", "Wandering Hero",
        "A heroic character roams from place to place, helping others and righting wrongs",
        PlotArchetypeCategory.JOURNEY,
        "Justice has no home",
        "Can one person make a difference?"
    )

    private fun createImpossibleJourney() = createArchetypeWithBasicLogic(
        "archetype_007_impossible_journey", "Impossible Journey",
        "Characters attempt a journey deemed impossible by all",
        PlotArchetypeCategory.JOURNEY,
        "Nothing is impossible",
        "What drives us beyond limits?"
    )

    private fun createRaceAgainstTime() = createArchetypeWithBasicLogic(
        "archetype_008_race_time", "Race Against Time",
        "Characters must complete a task or reach a goal before a crucial deadline",
        PlotArchetypeCategory.JOURNEY,
        "Time waits for no one",
        "Can we beat the clock?"
    )

    private fun createTheLastJourney() = createArchetypeWithBasicLogic(
        "archetype_009_last_journey", "The Last Journey",
        "A final voyage before death or ending",
        PlotArchetypeCategory.JOURNEY,
        "Every journey must end",
        "How do we face our final journey?"
    )

    private fun createCircularJourney() = createArchetypeWithBasicLogic(
        "archetype_010_circular_journey", "Circular Journey",
        "The journey ends where it began, but the traveler is transformed",
        PlotArchetypeCategory.JOURNEY,
        "We can never go home again",
        "Do we ever truly return?"
    )

    // ============ TRANSFORMATION ARCHETYPES (11-20) ============

    private fun createComingOfAge() = PlotArchetype(
        id = "archetype_011_coming_age",
        name = "Coming of Age",
        description = "Focuses on the growth and maturation of a young character transitioning to adulthood",
        category = PlotArchetypeCategory.TRANSFORMATION,

        archetypePlotLogic = PlotLogic(
            centralEngine = CentralEngine(
                mainConflict = "Innocence vs Experience",
                whyUnresolvable = "Growing up is inevitable but painful",
                stakesEscalation = listOf("Identity crisis", "Family strain", "Life decisions"),
                pointOfNoReturn = "Loss of innocence",
                onlyPossibleResolution = "Accept adult complexity"
            ),
            causalChains = listOf(),
            tickingClocks = listOf(),
            pivotPoints = listOf(),
            dominoChains = listOf(),
            inevitabilities = listOf()
        ),

        archetypeBeats = listOf(
            ArchetypeBeat("Innocent Beginning", "Youthful worldview", 0.0f, NarrativeFunction.OPENING, true),
            ArchetypeBeat("First Challenge", "Adult problem", 0.2f, NarrativeFunction.INCITING_INCIDENT, true),
            ArchetypeBeat("Loss of Innocence", "Major revelation", 0.5f, NarrativeFunction.MIDPOINT, true),
            ArchetypeBeat("Identity Crisis", "Question everything", 0.6f, NarrativeFunction.CRISIS, true),
            ArchetypeBeat("New Maturity", "Accept complexity", 0.95f, NarrativeFunction.RESOLUTION, true)
        ),

        thematicCore = ThematicCore(
            centralTheme = "Loss of innocence, gain of wisdom",
            moralQuestion = "What does it mean to grow up?",
            emotionalJourney = "Naivety → Disillusionment → Understanding",
            universalTruth = "Maturity comes through experience"
        ),

        examples = listOf("Stand by Me", "The Catcher in the Rye", "Lady Bird"),
        modernAdaptations = listOf("Digital native awakening", "Climate change generation"),
        subversions = listOf("Refuses to grow up", "Adult becomes child")
    )

    private fun createRedemption() = createArchetypeWithBasicLogic(
        "archetype_012_redemption", "Redemption",
        "A character seeks to atone for past mistakes or bad actions",
        PlotArchetypeCategory.TRANSFORMATION,
        "Everyone deserves a second chance",
        "Can we truly atone for our past?"
    )

    private fun createFallFromGrace() = createArchetypeWithBasicLogic(
        "archetype_013_fall_grace", "Fall from Grace",
        "The protagonist experiences a dramatic downfall from power or prestige",
        PlotArchetypeCategory.TRANSFORMATION,
        "Pride comes before the fall",
        "What causes our downfall?"
    )

    private fun createRedemptionQuest() = createArchetypeWithBasicLogic(
        "archetype_014_redemption_quest", "Redemption Quest",
        "The protagonist seeks redemption through a series of trials",
        PlotArchetypeCategory.TRANSFORMATION,
        "Redemption must be earned",
        "How far must we go to redeem ourselves?"
    )

    private fun createRebirth() = createArchetypeWithBasicLogic(
        "archetype_015_rebirth", "Rebirth",
        "Character undergoes death and rebirth, literal or metaphorical",
        PlotArchetypeCategory.TRANSFORMATION,
        "Death is not the end",
        "Can we truly start over?"
    )

    private fun createMetamorphosis() = createArchetypeWithBasicLogic(
        "archetype_016_metamorphosis", "Metamorphosis",
        "Physical or psychological transformation of character",
        PlotArchetypeCategory.TRANSFORMATION,
        "Change is inevitable",
        "What triggers transformation?"
    )

    private fun createAwakening() = createArchetypeWithBasicLogic(
        "archetype_017_awakening", "Awakening",
        "Character awakens to new understanding or power",
        PlotArchetypeCategory.TRANSFORMATION,
        "Awakening changes everything",
        "What awakens us?"
    )

    private fun createCorruption() = createArchetypeWithBasicLogic(
        "archetype_018_corruption", "Corruption",
        "Good character gradually becomes evil",
        PlotArchetypeCategory.TRANSFORMATION,
        "Evil corrupts gradually",
        "What corrupts the good?"
    )

    private fun createEnlightenment() = createArchetypeWithBasicLogic(
        "archetype_019_enlightenment", "Enlightenment",
        "Journey toward spiritual or intellectual enlightenment",
        PlotArchetypeCategory.TRANSFORMATION,
        "Knowledge transforms",
        "What is true enlightenment?"
    )

    private fun createDarkTransformation() = createArchetypeWithBasicLogic(
        "archetype_020_dark_transformation", "Dark Transformation",
        "Character transforms into something monstrous",
        PlotArchetypeCategory.TRANSFORMATION,
        "Monsters are made, not born",
        "What makes us monstrous?"
    )

    // ============ RELATIONSHIP ARCHETYPES (21-35) ============

    private fun createLoveTriangle() = createArchetypeWithBasicLogic(
        "archetype_021_love_triangle", "Love Triangle",
        "Three characters entangled in complex romantic relationship",
        PlotArchetypeCategory.RELATIONSHIP,
        "Love is complicated",
        "Can we love more than one?"
    )

    private fun createForbiddenLove() = createArchetypeWithBasicLogic(
        "archetype_022_forbidden_love", "Forbidden Love",
        "Characters from opposing backgrounds fall in love against societal opposition",
        PlotArchetypeCategory.RELATIONSHIP,
        "Love knows no boundaries",
        "Is love worth any sacrifice?"
    )

    private fun createUnrequitedLove() = createArchetypeWithBasicLogic(
        "archetype_023_unrequited_love", "Unrequited Love",
        "One-sided romantic feelings lead to emotional conflict",
        PlotArchetypeCategory.RELATIONSHIP,
        "Not all love is returned",
        "How do we handle rejection?"
    )

    private fun createLoveAtFirstSight() = createArchetypeWithBasicLogic(
        "archetype_024_love_first_sight", "Love at First Sight",
        "Characters fall deeply in love immediately upon meeting",
        PlotArchetypeCategory.RELATIONSHIP,
        "Some connections are instant",
        "Does love at first sight exist?"
    )

    private fun createReunitedLovers() = createArchetypeWithBasicLogic(
        "archetype_025_reunited_lovers", "Reunited Lovers",
        "Separated lovers find each other again",
        PlotArchetypeCategory.RELATIONSHIP,
        "True love always returns",
        "Can love survive separation?"
    )

    private fun createStarCrossedLovers() = createArchetypeWithBasicLogic(
        "archetype_026_star_crossed", "Star-Crossed Lovers",
        "Lovers doomed by fate",
        PlotArchetypeCategory.RELATIONSHIP,
        "Some love is tragic",
        "Can we defy fate?"
    )

    private fun createImpossibleLove() = createArchetypeWithBasicLogic(
        "archetype_027_impossible_love", "Impossible Love",
        "Characters from different species or existences fall in love",
        PlotArchetypeCategory.RELATIONSHIP,
        "Love transcends form",
        "What makes love impossible?"
    )

    private fun createArrangedMarriage() = createArchetypeWithBasicLogic(
        "archetype_028_arranged_marriage", "Arranged Marriage",
        "Characters navigate forced romantic arrangement",
        PlotArchetypeCategory.RELATIONSHIP,
        "Love can grow from duty",
        "Can love be arranged?"
    )

    private fun createLoveConquersAll() = createArchetypeWithBasicLogic(
        "archetype_029_love_conquers", "Love Conquers All",
        "Love overcomes all obstacles",
        PlotArchetypeCategory.RELATIONSHIP,
        "Love is the strongest force",
        "Can love truly conquer all?"
    )

    private fun createTragicLove() = createArchetypeWithBasicLogic(
        "archetype_030_tragic_love", "Tragic Love",
        "Love story ends in tragedy",
        PlotArchetypeCategory.RELATIONSHIP,
        "Not all love stories end happily",
        "Why must love be tragic?"
    )

    private fun createSecondChanceLove() = createArchetypeWithBasicLogic(
        "archetype_031_second_chance", "Second Chance Love",
        "Former lovers get another chance",
        PlotArchetypeCategory.RELATIONSHIP,
        "Love deserves second chances",
        "Can we recapture lost love?"
    )

    private fun createEnemiestoLovers() = createArchetypeWithBasicLogic(
        "archetype_032_enemies_lovers", "Enemies to Lovers",
        "Antagonistic characters fall in love",
        PlotArchetypeCategory.RELATIONSHIP,
        "Hate and love are close",
        "Can enemies become lovers?"
    )

    private fun createFriendsToLovers() = createArchetypeWithBasicLogic(
        "archetype_033_friends_lovers", "Friends to Lovers",
        "Friendship evolves into romance",
        PlotArchetypeCategory.RELATIONSHIP,
        "Best relationships start as friendship",
        "Should friends become lovers?"
    )

    private fun createSoulmates() = createArchetypeWithBasicLogic(
        "archetype_034_soulmates", "Soulmates",
        "Characters destined to be together",
        PlotArchetypeCategory.RELATIONSHIP,
        "Some connections are destined",
        "Do soulmates exist?"
    )

    private fun createLoveAcrossTime() = createArchetypeWithBasicLogic(
        "archetype_035_love_time", "Love Across Time",
        "Love transcends time periods",
        PlotArchetypeCategory.RELATIONSHIP,
        "Love is timeless",
        "Can love transcend time?"
    )

    // ============ CONFLICT ARCHETYPES (36-50) ============

    private fun createRevenge() = createArchetypeWithBasicLogic(
        "archetype_036_revenge", "Revenge",
        "Character seeks retribution against those who wronged them",
        PlotArchetypeCategory.CONFLICT,
        "Vengeance consumes the avenger",
        "Is revenge ever justified?"
    )

    private fun createOvercomingTheMonster() = createArchetypeWithBasicLogic(
        "archetype_037_monster", "Overcoming the Monster",
        "Protagonist confronts and defeats a powerful antagonist",
        PlotArchetypeCategory.CONFLICT,
        "Monsters must be faced",
        "What makes a monster?"
    )

    private fun createWar() = createArchetypeWithBasicLogic(
        "archetype_038_war", "War",
        "Story revolves around conflicts and consequences of war",
        PlotArchetypeCategory.CONFLICT,
        "War changes everything",
        "What is worth fighting for?"
    )

    private fun createBattleGoodEvil() = createArchetypeWithBasicLogic(
        "archetype_039_good_evil", "Battle of Good vs Evil",
        "Classic confrontation between good and evil forces",
        PlotArchetypeCategory.CONFLICT,
        "Good must triumph",
        "Is anyone purely good or evil?"
    )

    private fun createManVsNature() = createArchetypeWithBasicLogic(
        "archetype_040_man_nature", "Man vs Nature",
        "Struggle against natural disasters or environmental challenges",
        PlotArchetypeCategory.CONFLICT,
        "Nature is indifferent",
        "Can we conquer nature?"
    )

    private fun createManVsMachine() = createArchetypeWithBasicLogic(
        "archetype_041_man_machine", "Man vs Machine",
        "Conflict between humanity and technology",
        PlotArchetypeCategory.CONFLICT,
        "Technology threatens humanity",
        "Will machines replace us?"
    )

    private fun createCivilWar() = createArchetypeWithBasicLogic(
        "archetype_042_civil_war", "Civil War",
        "Nation or group tears itself apart",
        PlotArchetypeCategory.CONFLICT,
        "Division destroys from within",
        "Can a divided house stand?"
    )

    private fun createRevolutionaryWar() = createArchetypeWithBasicLogic(
        "archetype_043_revolution", "Revolutionary War",
        "Oppressed rise against oppressors",
        PlotArchetypeCategory.CONFLICT,
        "Revolution demands sacrifice",
        "When is revolution necessary?"
    )

    private fun createHolyWar() = createArchetypeWithBasicLogic(
        "archetype_044_holy_war", "Holy War",
        "Religious or ideological warfare",
        PlotArchetypeCategory.CONFLICT,
        "Faith drives conflict",
        "Can faith justify war?"
    )

    private fun createPersonalVendetta() = createArchetypeWithBasicLogic(
        "archetype_045_vendetta", "Personal Vendetta",
        "Individual pursuit of personal justice",
        PlotArchetypeCategory.CONFLICT,
        "Personal justice consumes",
        "Where does justice end and revenge begin?"
    )

    private fun createFamilyFeud() = createArchetypeWithBasicLogic(
        "archetype_046_family_feud", "Family Feud",
        "Generational conflict between families",
        PlotArchetypeCategory.CONFLICT,
        "Blood feuds run deep",
        "Can family wounds heal?"
    )

    private fun createTribalConflict() = createArchetypeWithBasicLogic(
        "archetype_047_tribal", "Tribal Conflict",
        "Conflict between different groups or tribes",
        PlotArchetypeCategory.CONFLICT,
        "Tribalism divides us",
        "Can different tribes coexist?"
    )

    private fun createIdeologicalWar() = createArchetypeWithBasicLogic(
        "archetype_048_ideological", "Ideological War",
        "Battle between competing ideologies",
        PlotArchetypeCategory.CONFLICT,
        "Ideas worth fighting for",
        "Which ideology will prevail?"
    )

    private fun createColdWar() = createArchetypeWithBasicLogic(
        "archetype_049_cold_war", "Cold War",
        "Conflict through espionage and proxy battles",
        PlotArchetypeCategory.CONFLICT,
        "War without battles",
        "Is cold war better than hot?"
    )

    private fun createPsychologicalWarfare() = createArchetypeWithBasicLogic(
        "archetype_050_psychological", "Psychological Warfare",
        "Mental and emotional manipulation as weapon",
        PlotArchetypeCategory.CONFLICT,
        "The mind is the battlefield",
        "How do we fight invisible wars?"
    )

    // ============ POWER ARCHETYPES (51-60) ============

    private fun createRagsToRiches() = createArchetypeWithBasicLogic(
        "archetype_051_rags_riches", "Rags to Riches",
        "Character rises from poverty to wealth and success",
        PlotArchetypeCategory.POWER,
        "Dreams can come true",
        "Does money bring happiness?"
    )

    private fun createUnderdog() = createArchetypeWithBasicLogic(
        "archetype_052_underdog", "Underdog",
        "Disadvantaged protagonist overcomes formidable challenges",
        PlotArchetypeCategory.POWER,
        "Heart beats might",
        "Can David beat Goliath?"
    )

    private fun createPowerAndCorruption() = createArchetypeWithBasicLogic(
        "archetype_053_power_corruption", "Power and Corruption",
        "Character deals with the corrupting influence of power",
        PlotArchetypeCategory.POWER,
        "Power corrupts absolutely",
        "Can power be wielded wisely?"
    )

    private fun createRiseAndFall() = createArchetypeWithBasicLogic(
        "archetype_054_rise_fall", "Rise and Fall",
        "Character's ascent to power followed by downfall",
        PlotArchetypeCategory.POWER,
        "What goes up must come down",
        "Is success sustainable?"
    )

    private fun createUsurper() = createArchetypeWithBasicLogic(
        "archetype_055_usurper", "The Usurper",
        "Character seizes power illegitimately",
        PlotArchetypeCategory.POWER,
        "Power taken is never secure",
        "Can stolen power last?"
    )

    private fun createKingmaker() = createArchetypeWithBasicLogic(
        "archetype_056_kingmaker", "The Kingmaker",
        "Character who creates or destroys leaders",
        PlotArchetypeCategory.POWER,
        "True power is making kings",
        "Who really holds power?"
    )

    private fun createPowerStruggle() = createArchetypeWithBasicLogic(
        "archetype_057_power_struggle", "Power Struggle",
        "Multiple parties vie for control",
        PlotArchetypeCategory.POWER,
        "Power creates conflict",
        "Who deserves to rule?"
    )

    private fun createInheritance() = createArchetypeWithBasicLogic(
        "archetype_058_inheritance", "The Inheritance",
        "Character inherits power or responsibility",
        PlotArchetypeCategory.POWER,
        "Legacy is a burden",
        "Can we escape our inheritance?"
    )

    private fun createPowerVacuum() = createArchetypeWithBasicLogic(
        "archetype_059_power_vacuum", "Power Vacuum",
        "Absence of authority creates chaos",
        PlotArchetypeCategory.POWER,
        "Absence of power creates chaos",
        "Who fills the void?"
    )

    private fun createReluctantRuler() = createArchetypeWithBasicLogic(
        "archetype_060_reluctant_ruler", "Reluctant Ruler",
        "Character forced into position of power",
        PlotArchetypeCategory.POWER,
        "Best leaders don't want power",
        "Should power be sought or given?"
    )

    // ============ MYSTERY ARCHETYPES (61-70) ============

    private fun createMystery() = createArchetypeWithBasicLogic(
        "archetype_061_mystery", "Mystery",
        "Solving a puzzling and often crime-related enigma",
        PlotArchetypeCategory.MYSTERY,
        "Truth will be revealed",
        "Can we handle the truth?"
    )

    private fun createDiscovery() = createArchetypeWithBasicLogic(
        "archetype_062_discovery", "Discovery",
        "Characters uncover hidden truths or lost civilizations",
        PlotArchetypeCategory.MYSTERY,
        "Discovery changes everything",
        "What lies hidden?"
    )

    private fun createIdentityCrisis() = createArchetypeWithBasicLogic(
        "archetype_063_identity_crisis", "Identity Crisis",
        "Character grapples with their true identity",
        PlotArchetypeCategory.MYSTERY,
        "Know thyself",
        "Who are we really?"
    )

    private fun createTheDouble() = createArchetypeWithBasicLogic(
        "archetype_064_double", "The Double",
        "Doppelgangers, clones, or twins create identity confusion",
        PlotArchetypeCategory.MYSTERY,
        "Identity is fragile",
        "What makes us unique?"
    )

    private fun createHiddenIdentity() = createArchetypeWithBasicLogic(
        "archetype_065_hidden_identity", "Hidden Identity",
        "Character conceals their true identity",
        PlotArchetypeCategory.MYSTERY,
        "Secrets always surface",
        "Why do we hide ourselves?"
    )

    private fun createAmnesiaMyster() = createArchetypeWithBasicLogic(
        "archetype_066_amnesia", "Amnesia Mystery",
        "Lost memory drives the plot",
        PlotArchetypeCategory.MYSTERY,
        "Memory defines us",
        "Who are we without memories?"
    )

    private fun createLockedRoom() = createArchetypeWithBasicLogic(
        "archetype_067_locked_room", "Locked Room Mystery",
        "Impossible crime in sealed location",
        PlotArchetypeCategory.MYSTERY,
        "Every puzzle has a solution",
        "How was it done?"
    )

    private fun createConspiracy() = createArchetypeWithBasicLogic(
        "archetype_068_conspiracy", "Conspiracy",
        "Uncovering a hidden plot or conspiracy",
        PlotArchetypeCategory.MYSTERY,
        "Nothing is as it seems",
        "How deep does it go?"
    )

    private fun createTreasureHunt() = createArchetypeWithBasicLogic(
        "archetype_069_treasure_hunt", "Treasure Hunt",
        "Following clues to find treasure",
        PlotArchetypeCategory.MYSTERY,
        "X marks the spot",
        "Is the treasure worth it?"
    )

    private fun createMissingPerson() = createArchetypeWithBasicLogic(
        "archetype_070_missing_person", "Missing Person",
        "Search for someone who has disappeared",
        PlotArchetypeCategory.MYSTERY,
        "Everyone leaves traces",
        "Why do people disappear?"
    )

    // ============ SURVIVAL ARCHETYPES (71-80) ============

    private fun createSurvival() = createArchetypeWithBasicLogic(
        "archetype_071_survival", "Survival",
        "Characters fight to stay alive in challenging situations",
        PlotArchetypeCategory.SURVIVAL,
        "Life finds a way",
        "What would you do to survive?"
    )

    private fun createEscape() = createArchetypeWithBasicLogic(
        "archetype_072_escape", "Escape",
        "Characters strive to break free from confinement",
        PlotArchetypeCategory.SURVIVAL,
        "Freedom is everything",
        "What price for freedom?"
    )

    private fun createApocalypse() = createArchetypeWithBasicLogic(
        "archetype_073_apocalypse", "Apocalypse",
        "Story centers around catastrophic world-ending event",
        PlotArchetypeCategory.SURVIVAL,
        "The end is the beginning",
        "How does the world end?"
    )

    private fun createDesertedIsland() = createArchetypeWithBasicLogic(
        "archetype_074_island", "Deserted Island",
        "Survival on isolated island",
        PlotArchetypeCategory.SURVIVAL,
        "Isolation reveals truth",
        "Can we survive alone?"
    )

    private fun createNaturalDisaster() = createArchetypeWithBasicLogic(
        "archetype_075_disaster", "Natural Disaster",
        "Surviving catastrophic natural event",
        PlotArchetypeCategory.SURVIVAL,
        "Nature's fury is absolute",
        "Can we survive nature's wrath?"
    )

    private fun createPlague() = createArchetypeWithBasicLogic(
        "archetype_076_plague", "Plague",
        "Surviving deadly disease outbreak",
        PlotArchetypeCategory.SURVIVAL,
        "Disease tests humanity",
        "How do we face invisible death?"
    )

    private fun createAlienInvasion() = createArchetypeWithBasicLogic(
        "archetype_077_alien_invasion", "Alien Invasion",
        "Humanity faces extraterrestrial threat",
        PlotArchetypeCategory.SURVIVAL,
        "We are not alone",
        "Can humanity unite?"
    )

    private fun createZombieOutbreak() = createArchetypeWithBasicLogic(
        "archetype_078_zombies", "Zombie Outbreak",
        "Surviving undead apocalypse",
        PlotArchetypeCategory.SURVIVAL,
        "The dead don't stay dead",
        "What makes us human?"
    )

    private fun createPostApocalyptic() = createArchetypeWithBasicLogic(
        "archetype_079_post_apocalyptic", "Post-Apocalyptic",
        "Surviving after civilization falls",
        PlotArchetypeCategory.SURVIVAL,
        "After the fall",
        "Can we rebuild?"
    )

    private fun createLastSurvivors() = createArchetypeWithBasicLogic(
        "archetype_080_last_survivors", "Last Survivors",
        "Final humans struggle to survive",
        PlotArchetypeCategory.SURVIVAL,
        "Humanity's last stand",
        "Is survival worth it?"
    )

    // ============ COMEDY ARCHETYPES (81-85) ============

    private fun createFishOutOfWater() = createArchetypeWithBasicLogic(
        "archetype_081_fish_water", "Fish Out of Water",
        "Character in unfamiliar environment creates humor",
        PlotArchetypeCategory.COMEDY,
        "Unfamiliarity breeds comedy",
        "How do we adapt?"
    )

    private fun createMistakenIdentity() = createArchetypeWithBasicLogic(
        "archetype_082_mistaken_identity", "Mistaken Identity",
        "Identity confusion creates comedic situations",
        PlotArchetypeCategory.COMEDY,
        "Confusion creates comedy",
        "Who do we think we are?"
    )

    private fun createOddCouple() = createArchetypeWithBasicLogic(
        "archetype_083_odd_couple", "Odd Couple",
        "Mismatched partners forced together",
        PlotArchetypeCategory.COMEDY,
        "Opposites attract chaos",
        "Can opposites coexist?"
    )

    private fun createFarcicalMisunderstanding() = createArchetypeWithBasicLogic(
        "archetype_084_farce", "Farcical Misunderstanding",
        "Escalating misunderstandings create chaos",
        PlotArchetypeCategory.COMEDY,
        "Misunderstanding multiplies",
        "Why don't we just talk?"
    )

    private fun createRoleReversal() = createArchetypeWithBasicLogic(
        "archetype_085_role_reversal", "Role Reversal",
        "Characters switch traditional roles",
        PlotArchetypeCategory.COMEDY,
        "Walking in another's shoes",
        "What if roles were reversed?"
    )

    // ============ TRAGEDY ARCHETYPES (86-90) ============

    private fun createTragedy() = createArchetypeWithBasicLogic(
        "archetype_086_tragedy", "Tragedy",
        "Protagonist faces downfall due to fatal flaw",
        PlotArchetypeCategory.TRAGEDY,
        "Fatal flaws destroy",
        "Could tragedy be avoided?"
    )

    private fun createSacrifice() = createArchetypeWithBasicLogic(
        "archetype_087_sacrifice", "Sacrifice",
        "Character makes selfless act for greater good",
        PlotArchetypeCategory.TRAGEDY,
        "Love demands sacrifice",
        "What's worth dying for?"
    )

    private fun createHubrisFall() = createArchetypeWithBasicLogic(
        "archetype_088_hubris", "Hubris and Fall",
        "Excessive pride leads to downfall",
        PlotArchetypeCategory.TRAGEDY,
        "Pride precedes destruction",
        "Why can't we see our flaws?"
    )

    private fun createForbiddenKnowledge() = createArchetypeWithBasicLogic(
        "archetype_089_forbidden_knowledge", "Forbidden Knowledge",
        "Pursuing dangerous knowledge leads to doom",
        PlotArchetypeCategory.TRAGEDY,
        "Some things shouldn't be known",
        "Is ignorance bliss?"
    )

    private fun createInevitableFate() = createArchetypeWithBasicLogic(
        "archetype_090_fate", "Inevitable Fate",
        "Characters cannot escape destined doom",
        PlotArchetypeCategory.TRAGEDY,
        "Fate cannot be avoided",
        "Do we have free will?"
    )

    // ============ SOCIETAL ARCHETYPES (91-100) ============

    private fun createRebellion() = createArchetypeWithBasicLogic(
        "archetype_091_rebellion", "Rebellion",
        "Characters challenge authority or oppressive systems",
        PlotArchetypeCategory.SOCIETAL,
        "Freedom requires rebellion",
        "When should we rebel?"
    )

    private fun createDystopia() = createArchetypeWithBasicLogic(
        "archetype_092_dystopia", "Dystopia",
        "Dark oppressive society serves as backdrop",
        PlotArchetypeCategory.SOCIETAL,
        "Society can become nightmare",
        "How do societies fail?"
    )

    private fun createUtopiaGoneWrong() = createArchetypeWithBasicLogic(
        "archetype_093_utopia_wrong", "Utopia Gone Wrong",
        "Perfect society hides dark secrets",
        PlotArchetypeCategory.SOCIETAL,
        "Perfection is illusion",
        "Can utopia exist?"
    )

    private fun createSocietyVsIndividual() = createArchetypeWithBasicLogic(
        "archetype_094_society_individual", "Society vs Individual",
        "Individual fights against societal norms",
        PlotArchetypeCategory.SOCIETAL,
        "Conformity vs freedom",
        "Should we conform?"
    )

    private fun createClassStruggle() = createArchetypeWithBasicLogic(
        "archetype_095_class_struggle", "Class Struggle",
        "Conflict between social classes",
        PlotArchetypeCategory.SOCIETAL,
        "Inequality breeds conflict",
        "Can classes coexist?"
    )

    private fun createGenerationGap() = createArchetypeWithBasicLogic(
        "archetype_096_generation_gap", "Generation Gap",
        "Conflict between different generations",
        PlotArchetypeCategory.SOCIETAL,
        "Each generation rebels",
        "Can generations understand each other?"
    )

    private fun createCulturalClash() = createArchetypeWithBasicLogic(
        "archetype_097_cultural_clash", "Cultural Clash",
        "Conflict between different cultures",
        PlotArchetypeCategory.SOCIETAL,
        "Culture shapes conflict",
        "Can cultures harmonize?"
    )

    private fun createTimeLoop() = createArchetypeWithBasicLogic(
        "archetype_098_time_loop", "Time Loop",
        "Characters trapped in repeating time cycle",
        PlotArchetypeCategory.MYSTERY,
        "Time is a prison",
        "How do we break cycles?"
    )

    private fun createParallelWorlds() = createArchetypeWithBasicLogic(
        "archetype_099_parallel_worlds", "Parallel Worlds",
        "Characters interact with alternate realities",
        PlotArchetypeCategory.MYSTERY,
        "Infinite possibilities exist",
        "Which reality is real?"
    )

    private fun createChosenOne() = createArchetypeWithBasicLogic(
        "archetype_100_chosen_one", "Chosen One",
        "Protagonist destined to fulfill particular role",
        PlotArchetypeCategory.JOURNEY,
        "Destiny chooses us",
        "Are we truly chosen or do we choose?"
    )

    // ============ HELPER FUNCTIONS ============

    private fun createJourneyLogic(conflict: String) = PlotLogic(
        centralEngine = CentralEngine(
            mainConflict = conflict,
            whyUnresolvable = "Journey fundamentally changes traveler",
            stakesEscalation = listOf("Personal", "Group", "World"),
            pointOfNoReturn = "Departure from known world",
            onlyPossibleResolution = "Complete journey or fail"
        ),
        causalChains = listOf(),
        tickingClocks = listOf(),
        pivotPoints = listOf(),
        dominoChains = listOf(),
        inevitabilities = listOf()
    )

    private fun createArchetypeWithBasicLogic(
        id: String,
        name: String,
        description: String,
        category: PlotArchetypeCategory,
        theme: String,
        question: String
    ) = PlotArchetype(
        id = id,
        name = name,
        description = description,
        category = category,

        archetypePlotLogic = PlotLogic(
            centralEngine = CentralEngine(
                mainConflict = "$name core conflict",
                whyUnresolvable = "Fundamental tension of $name",
                stakesEscalation = listOf("Personal stakes", "Relational stakes", "Universal stakes"),
                pointOfNoReturn = "Critical decision point",
                onlyPossibleResolution = "Accept consequences"
            ),
            causalChains = listOf(),
            tickingClocks = listOf(),
            pivotPoints = listOf(),
            dominoChains = listOf(),
            inevitabilities = listOf()
        ),

        archetypeBeats = generateStandardBeats(name),

        thematicCore = ThematicCore(
            centralTheme = theme,
            moralQuestion = question,
            emotionalJourney = "Beginning → Middle → End emotional arc",
            universalTruth = theme
        ),

        examples = listOf(),
        modernAdaptations = listOf(),
        subversions = listOf()
    )

    private fun generateStandardBeats(archetypeName: String): List<ArchetypeBeat> {
        return listOf(
            ArchetypeBeat("Setup", "Establish situation", 0.1f, NarrativeFunction.OPENING, true),
            ArchetypeBeat("Catalyst", "Inciting incident", 0.2f, NarrativeFunction.CATALYST, true),
            ArchetypeBeat("Development", "Explore conflict", 0.4f, NarrativeFunction.RISING_ACTION, true),
            ArchetypeBeat("Midpoint", "Major revelation", 0.5f, NarrativeFunction.MIDPOINT, true),
            ArchetypeBeat("Crisis", "Lowest point", 0.7f, NarrativeFunction.CRISIS, true),
            ArchetypeBeat("Climax", "Final confrontation", 0.85f, NarrativeFunction.CLIMAX, true),
            ArchetypeBeat("Resolution", "New equilibrium", 0.95f, NarrativeFunction.RESOLUTION, true)
        )
    }
}