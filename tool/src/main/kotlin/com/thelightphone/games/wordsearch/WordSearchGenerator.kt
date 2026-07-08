package com.thelightphone.games.wordsearch

import kotlin.random.Random

/** A word placed on the grid, recorded as its ordered list of (row, col) cells. */
data class PlacedWord(
    val word: String,
    val startRow: Int,
    val startCol: Int,
    val dRow: Int,
    val dCol: Int,
) {
    val cells: List<Pair<Int, Int>> =
        word.indices.map { i -> (startRow + dRow * i) to (startCol + dCol * i) }
}

class WordSearchPuzzle(
    val size: Int,
    val grid: Array<CharArray>,
    val words: List<PlacedWord>,
)

object WordSearchGenerator {

    // All 8 compass directions, so words can run in any straight line (incl. diagonals, reversed).
    private val DIRECTIONS = listOf(
        0 to 1, 1 to 0, 1 to 1, -1 to 1,
        0 to -1, -1 to 0, -1 to -1, 1 to -1,
    )

    private val WORD_BANK = listOf(
        // Animals
        "LION", "TIGER", "ZEBRA", "GIRAFFE", "ELEPHANT", "PANDA", "KOALA", "KANGAROO",
        "CHEETAH", "LEOPARD", "JAGUAR", "GORILLA", "CHIMPANZEE", "OCELOT", "MEERKAT", "OTTER",
        "BEAVER", "RACCOON", "SQUIRREL", "HEDGEHOG", "PORCUPINE", "ARMADILLO", "ANTEATER", "SLOTH",
        "TAPIR", "BISON", "BUFFALO", "ANTELOPE", "GAZELLE", "IMPALA", "WILDEBEEST", "HYENA",
        "JACKAL", "MONGOOSE", "WOLVERINE", "LYNX", "BOBCAT", "COUGAR", "PUMA", "CARIBOU",
        "MOOSE", "ALPACA", "LLAMA", "CAMEL", "DONKEY", "MULE", "RHINOCEROS", "CROCODILE",
        "ALLIGATOR", "IGUANA", "CHAMELEON", "GECKO", "TORTOISE", "TURTLE", "PYTHON", "COBRA",
        "VIPER", "ANACONDA", "FLAMINGO", "PELICAN", "TOUCAN", "PARROT", "MACAW", "PEACOCK",
        "OSTRICH", "PENGUIN", "ALBATROSS", "FALCON", "EAGLE", "HAWK", "RAVEN", "CARDINAL",
        "BLUEJAY", "HUMMINGBIRD", "WOODPECKER", "KINGFISHER", "PUFFIN", "SEAGULL", "NARWHAL", "WALRUS",
        "OCTOPUS", "JELLYFISH", "STARFISH", "SEAHORSE", "LOBSTER", "STINGRAY", "PIRANHA", "CATFISH",
        "MACKEREL", "SWORDFISH", "DOLPHIN", "SPARROW",
        // Food
        "PANCAKE", "WAFFLE", "OMELET", "BURRITO", "TACO", "ENCHILADA", "QUESADILLA", "TAMALE",
        "CEVICHE", "PAELLA", "RISOTTO", "LASAGNA", "RAVIOLI", "GNOCCHI", "CANNOLI", "TIRAMISU",
        "BAGUETTE", "CROISSANT", "QUICHE", "RATATOUILLE", "FONDUE", "PRETZEL", "STRUDEL", "SCHNITZEL",
        "BRATWURST", "HUMMUS", "FALAFEL", "SHAWARMA", "KEBAB", "BAKLAVA", "COUSCOUS", "TAGINE",
        "CURRY", "MASALA", "BIRYANI", "SAMOSA", "CHUTNEY", "NAAN", "DUMPLING", "NOODLES",
        "RAMEN", "SUSHI", "SASHIMI", "TEMPURA", "TERIYAKI", "MISO", "KIMCHI", "BIBIMBAP",
        "PADTHAI", "PHO", "MANGO", "PAPAYA", "GUAVA", "LYCHEE", "POMEGRANATE", "PINEAPPLE",
        "COCONUT", "PLANTAIN", "CASSAVA", "YAM", "JACKFRUIT", "DURIAN", "PERSIMMON", "APRICOT",
        "NECTARINE", "CANTALOUPE", "HONEYDEW", "CRANBERRY", "BLUEBERRY", "RASPBERRY", "STRAWBERRY", "BLACKBERRY",
        "BREAD",
        // Vacation & travel
        "PASSPORT", "LUGGAGE", "SUITCASE", "BACKPACK", "ITINERARY", "SOUVENIR", "POSTCARD", "CAMPSITE",
        "HAMMOCK", "SNORKEL", "KAYAK", "CANOE", "CRUISE", "FERRY", "AIRPORT", "TERMINAL",
        "LAYOVER", "HOSTEL", "RESORT", "BEACH", "COASTLINE", "BOARDWALK", "LIGHTHOUSE", "CAMPFIRE",
        "TENT", "CAMPGROUND", "TRAILHEAD", "COMPASS", "BINOCULARS", "ADVENTURE", "EXCURSION", "SIGHTSEEING",
        "LANDMARK", "MUSEUM", "GALLERY", "MONUMENT", "CATHEDRAL", "TEMPLE", "SHRINE", "PALACE",
        "CASTLE", "FORTRESS", "CITADEL", "PLAZA", "MARKETPLACE", "BAZAAR", "CARAVAN", "VOYAGE",
        "JOURNEY",
        // Places (countries)
        "JAPAN", "CHINA", "INDIA", "KENYA", "GHANA", "NIGERIA", "EGYPT", "MOROCCO",
        "ETHIOPIA", "TANZANIA", "UGANDA", "ZAMBIA", "BOTSWANA", "NAMIBIA", "SENEGAL", "ALGERIA",
        "TUNISIA", "ANGOLA", "RWANDA", "SOMALIA", "ERITREA", "MALAWI", "ZIMBABWE", "MOZAMBIQUE",
        "CAMEROON", "LIBERIA", "GABON", "TOGO", "BENIN", "GUINEA", "MALI", "NIGER",
        "CHAD", "SUDAN", "VIETNAM", "THAILAND", "CAMBODIA", "MYANMAR", "MALAYSIA", "INDONESIA",
        "PHILIPPINES", "SINGAPORE", "MONGOLIA", "NEPAL", "BHUTAN", "BANGLADESH", "PAKISTAN", "JORDAN",
        "LEBANON", "SYRIA", "YEMEN", "OMAN", "QATAR", "KUWAIT", "BAHRAIN", "ISRAEL",
        "PALESTINE", "TURKEY", "ARMENIA", "GEORGIA", "AZERBAIJAN", "KAZAKHSTAN", "UZBEKISTAN", "MEXICO",
        "GUATEMALA", "HONDURAS", "NICARAGUA", "PANAMA", "COLOMBIA", "VENEZUELA", "ECUADOR", "PERU",
        "BOLIVIA", "PARAGUAY", "URUGUAY", "ARGENTINA", "BRAZIL", "CHILE", "CUBA", "JAMAICA",
        "HAITI", "BARBADOS", "BAHAMAS", "TRINIDAD", "FIJI", "SAMOA", "TONGA", "VANUATU",
        "AUSTRALIA", "CANADA", "ICELAND", "FINLAND", "NORWAY", "SWEDEN", "DENMARK", "IRELAND",
        "SCOTLAND", "PORTUGAL", "GREECE", "CROATIA", "SERBIA", "POLAND", "ROMANIA", "BULGARIA",
        "UKRAINE", "FRANCE", "GERMANY", "ITALY", "SPAIN",
        // Places (cities & landmarks)
        "NAIROBI", "LAGOS", "CAIRO", "MARRAKESH", "CASABLANCA", "ACCRA", "DAKAR", "KAMPALA",
        "MUMBAI", "DELHI", "BANGKOK", "JAKARTA", "MANILA", "HANOI", "SEOUL", "TOKYO",
        "KYOTO", "OSAKA", "BEIJING", "SHANGHAI", "HONGKONG", "ISTANBUL", "DUBAI", "DOHA",
        "RIYADH", "AMMAN", "BEIRUT", "JERUSALEM", "ATHENS", "ROME", "PARIS", "LONDON",
        "DUBLIN", "LISBON", "MADRID", "BARCELONA", "BERLIN", "VIENNA", "PRAGUE", "BUDAPEST",
        "WARSAW", "MOSCOW", "HAVANA", "KINGSTON", "BOGOTA", "LIMA", "SANTIAGO", "SAOPAULO",
        "TORONTO", "MONTREAL", "SYDNEY", "MELBOURNE", "AUCKLAND",
        // Geography features
        "MOUNTAIN", "VOLCANO", "GLACIER", "CANYON", "PLATEAU", "VALLEY", "DESERT", "SAVANNA",
        "TUNDRA", "RAINFOREST", "WETLAND", "MARSH", "SWAMP", "DELTA", "ESTUARY", "PENINSULA",
        "ARCHIPELAGO", "ISTHMUS", "FJORD", "LAGOON", "REEF", "ATOLL", "DUNE", "OASIS",
        "PLAIN", "PRAIRIE", "STEPPE", "HIGHLAND", "LOWLAND", "RIDGE", "SUMMIT", "CLIFF",
        "CAVERN", "GORGE", "WATERFALL", "GEYSER", "CRATER", "EQUATOR", "TROPICS", "HEMISPHERE",
        "CONTINENT",
        // Holidays (global)
        "DIWALI", "HOLI", "RAMADAN", "HANUKKAH", "PASSOVER", "KWANZAA", "NOWRUZ", "JUNETEENTH",
        "OKTOBERFEST", "MIDSUMMER", "SONGKRAN", "CHUSEOK", "NAVRATRI", "ONAM", "JUNKANOO", "CARNIVAL",
        "VESAK", "OBON", "EASTER", "CHRISTMAS", "HALLOWEEN", "NEWYEAR", "TET", "BASTILLE",
        "POSADA",
        // Notable people of color (surnames)
        "MANDELA", "TUBMAN", "DOUGLASS", "PARKS", "HAMER", "BALDWIN", "HURSTON", "ANGELOU",
        "MORRISON", "HUGHES", "WELLS", "TRUTH", "BETHUNE", "CARVER", "BANNEKER", "CHISHOLM",
        "BUNCHE", "MARSHALL", "RICE", "OBAMA", "ALI", "ROBINSON", "OWENS", "ASHE",
        "JOYNER", "BOLT", "WILLIAMS", "ARMSTRONG", "ELLINGTON", "FITZGERALD", "HOLIDAY", "FRANKLIN",
        "MARLEY", "WONDER", "HENDRIX", "BASQUIAT", "LAWRENCE", "GANDHI", "YOUSAFZAI", "HUERTA",
        "RIVERA", "KAHLO", "MARQUEZ", "NERUDA", "CONFUCIUS", "TAGORE", "SUZUKI",
        // Nature & weather
        "RAINBOW", "THUNDER", "LIGHTNING", "BLIZZARD", "HURRICANE", "TORNADO", "MONSOON", "DROUGHT",
        "AVALANCHE", "EARTHQUAKE", "TSUNAMI", "ECLIPSE", "METEOR", "COMET", "NEBULA", "GALAXY",
        "AURORA", "TWILIGHT", "SUNRISE", "SUNSET", "HORIZON", "BREEZE", "GALE", "FROST",
        "MIST", "FOG", "HAIL", "SLEET", "DRIZZLE",
        // Everyday / general
        "PENCIL", "CANDLE", "LANTERN", "PUZZLE", "MARBLE", "PEBBLE", "CRYSTAL", "VELVET",
        "WHISTLE", "BLOSSOM", "MEADOW", "HARBOR", "BRIDGE", "CHAIR", "GARDEN", "WINTER",
        "ISLAND", "FOREST", "PLANET", "ROCKET", "GUITAR", "PIANO", "UMBRELLA", "TELESCOPE",
        "MICROSCOPE", "HOURGLASS", "ANCHOR", "TREASURE", "OCEAN", "CLOUD", "APPLE", "RIVER",
        // Animals (more)
        "WOMBAT", "PLATYPUS", "DINGO", "QUOKKA", "CASSOWARY", "KIWI", "KOOKABURRA", "WALLABY",
        "BANDICOOT", "NUMBAT", "BILBY", "ECHIDNA", "MANATEE", "DUGONG", "BELUGA", "ORCAWHALE",
        "PORPOISE", "BARRACUDA", "MARLIN", "GROUPER", "HALIBUT", "FLOUNDER", "SNAPPER", "TILAPIA",
        "CARP", "MINNOW", "GUPPY", "CHINCHILLA", "GERBIL", "HAMSTER", "FERRET", "WEASEL",
        "STOAT", "MARTEN", "BADGER", "SKUNK", "OPOSSUM", "CAPYBARA", "AGOUTI", "CHIPMUNK",
        "MARMOT", "GROUNDHOG",
        // Food (more)
        "BORSCHT", "GOULASH", "PIEROGI", "STROGANOFF", "BRIOCHE", "CREPE", "ECLAIR", "MACARON",
        "PROFITEROLE", "FOCACCIA", "CIABATTA", "PITA", "FLATBREAD", "CORNBREAD", "BISCUIT", "SCONE",
        "MUFFIN", "DONUT", "CUPCAKE", "BROWNIE", "FUDGE", "TOFFEE", "CARAMEL", "NOUGAT",
        "MARZIPAN", "MERINGUE", "SORBET", "GELATO", "POPSICLE", "MILKSHAKE", "SMOOTHIE", "LEMONADE",
        "ICEDTEA", "ESPRESSO", "CAPPUCCINO", "LATTE", "MOCHA", "CIDER", "EGGNOG", "PUNCH",
        "SANGRIA", "MOJITO",
        // Sports
        "SOCCER", "BASKETBALL", "BASEBALL", "FOOTBALL", "HOCKEY", "CRICKET", "RUGBY", "TENNIS",
        "GOLF", "BOXING", "WRESTLING", "FENCING", "ARCHERY", "BADMINTON", "SQUASH", "BOWLING",
        "BILLIARDS", "DARTS", "CURLING", "LACROSSE", "VOLLEYBALL", "HANDBALL", "WATERPOLO", "ROWING",
        "SAILING", "SURFING", "SKATEBOARD", "SNOWBOARD", "SKIING", "LUGE", "BOBSLED", "SKELETON",
        "BIATHLON",
        // Music & instruments
        "VIOLIN", "CELLO", "VIOLA", "HARP", "TRUMPET", "TROMBONE", "CLARINET", "OBOE",
        "BASSOON", "FLUTE", "SAXOPHONE", "ACCORDION", "HARMONICA", "UKULELE", "BANJO", "MANDOLIN",
        "SITAR", "DIDGERIDOO", "BAGPIPES", "XYLOPHONE", "MARIMBA", "TIMPANI", "TAMBOURINE", "CASTANETS",
        "TRIANGLE", "CYMBAL", "DRUMKIT", "SYNTHESIZER",
        // Space & astronomy
        "QUASAR", "PULSAR", "ASTEROID", "METEORITE", "SATELLITE", "OBSERVATORY", "ASTRONAUT", "COSMONAUT",
        "SPACESHIP", "SHUTTLE", "ORBIT", "GRAVITY", "ATMOSPHERE", "EXOSPHERE", "MERCURY", "VENUS",
        "MARS", "JUPITER", "SATURN", "URANUS", "NEPTUNE", "PLUTO",
        // Professions
        "DOCTOR", "NURSE", "SURGEON", "DENTIST", "PHARMACIST", "THERAPIST", "ARCHITECT", "ENGINEER",
        "PLUMBER", "ELECTRICIAN", "CARPENTER", "MECHANIC", "WELDER", "BLACKSMITH", "TAILOR", "COBBLER",
        "BAKER", "BUTCHER", "FARMER", "FISHERMAN", "TEACHER", "PROFESSOR", "LIBRARIAN", "JOURNALIST",
        "EDITOR", "PUBLISHER", "PAINTER", "SCULPTOR", "MUSICIAN", "ACTOR", "DIRECTOR", "PRODUCER",
        // Transportation
        "BICYCLE", "MOTORCYCLE", "SCOOTER", "TRICYCLE", "UNICYCLE", "AUTOMOBILE", "TRUCK", "VAN",
        "TRAILER", "TRACTOR", "BULLDOZER", "FORKLIFT", "AMBULANCE", "FIRETRUCK", "TAXI", "LIMOUSINE",
        "RICKSHAW", "SUBWAY", "MONORAIL", "TRAM", "STREETCAR", "LOCOMOTIVE", "FREIGHT", "CARGO",
        "RAFT",
        // Mythology (global)
        "ZEUS", "ATHENA", "APOLLO", "ARTEMIS", "POSEIDON", "HERMES", "ARES", "APHRODITE",
        "HERA", "HADES", "ODIN", "THOR", "LOKI", "FREYA", "BALDER", "HEIMDALL",
        "VALKYRIE", "VALHALLA", "YGGDRASIL", "RAGNAROK", "ANUBIS", "OSIRIS", "ISIS", "HORUS",
        "SPHINX", "PHARAOH", "SCARAB",
        // Trees & plants
        "OAK", "MAPLE", "BIRCH", "WILLOW", "CEDAR", "SPRUCE", "PINE", "FIR",
        "REDWOOD", "SEQUOIA", "MAGNOLIA", "DOGWOOD", "SYCAMORE", "ELM", "ASH", "CHESTNUT",
        "WALNUT", "HICKORY", "POPLAR", "ALDER", "BAMBOO", "PALM",
        // Insects
        "BUTTERFLY", "DRAGONFLY", "LADYBUG", "BEETLE", "GRASSHOPPER", "CICADA", "MANTIS", "TERMITE",
        "WASP", "HORNET", "BUMBLEBEE", "FIREFLY", "MOSQUITO", "GNAT", "MOTH", "APHID",
        // Gemstones & minerals
        "DIAMOND", "RUBY", "EMERALD", "SAPPHIRE", "TOPAZ", "AMETHYST", "GARNET", "OPAL",
        "PEARL", "JADE", "ONYX", "QUARTZ", "AGATE", "TURQUOISE", "AQUAMARINE", "CITRINE",
        // Clothing & accessories
        "SWEATER", "JACKET", "BLAZER", "CARDIGAN", "HOODIE", "VEST", "PONCHO", "CLOAK",
        "CAPE", "OVERCOAT", "TROUSERS", "JEANS", "SHORTS", "LEGGINGS", "OVERALLS", "JUMPSUIT",
        "KIMONO", "SARI", "TURBAN", "SOMBRERO", "BERET", "FEDORA",
        // Places (more countries)
        "LATVIA", "LITHUANIA", "ESTONIA", "SLOVAKIA", "SLOVENIA", "MONTENEGRO", "KOSOVO", "MOLDOVA",
        "BELARUS", "ANDORRA", "MONACO", "MALTA", "CYPRUS", "LUXEMBOURG", "SANMARINO", "VATICAN",
        "BOSNIA", "MACEDONIA", "ALBANIA", "LAOS", "BRUNEI", "TIMOR", "MALDIVES", "SEYCHELLES",
        "MAURITIUS",
        // Places (more cities)
        "ABUJA", "KHARTOUM", "ALGIERS", "TRIPOLI", "TUNIS", "RABAT", "LUANDA", "MAPUTO",
        "HARARE", "LUSAKA", "GABORONE", "WINDHOEK", "PORTLOUIS", "VICTORIA", "COTONOU", "LOME",
        "OUAGADOUGOU", "BAMAKO", "NIAMEY", "DHAKA", "ISLAMABAD", "KABUL", "TEHRAN", "BAGHDAD",
        "DAMASCUS", "SANAA", "MUSCAT",
        // Holidays (more, global)
        "EPIPHANY", "ASSUMPTION", "ALLSAINTS", "CANDLEMAS", "PENTECOST", "ADVENT", "LENT", "PALMSUNDAY",
        "GOODFRIDAY", "QINGMING", "DUANWU", "MIDAUTUMN", "DOUBLENINTH", "HINAMATSURI", "SHICHIGOSAN",
        // Notable people of color (more surnames)
        "KEYS", "BEYONCE", "RIHANNA", "USHER", "PRINCE", "COSBY", "POITIER", "WASHINGTON",
        "FREEMAN", "GOLDBERG", "WINFREY", "LOVELACE", "JEMISON", "JOHNSON", "HAMILTON", "CATLETT",
        "COLEMAN", "LATIMER", "MCCOY", "JULIAN", "DREW", "MORGAN",
        // Geography (more features)
        "ICEBERG", "MARSHLAND", "BADLANDS", "MESA", "BUTTE", "RAVINE", "BLUFF", "FOOTHILLS",
        "WATERSHED", "TRIBUTARY",
        // Nature (more)
        "STALACTITE", "STALAGMITE", "GROTTO", "SINKHOLE", "PERMAFROST", "GLACIATION", "EROSION", "SEDIMENT",
        "MINERAL", "POLLINATION",
        // School & education
        "CLASSROOM", "CHALKBOARD", "BLACKBOARD", "TEXTBOOK", "NOTEBOOK", "CRAYON", "MARKER", "ERASER",
        "RULER", "PROTRACTOR", "CALCULATOR", "BEAKER", "TESTUBE", "FLASK",
    )
    /**
     * [excludeWords] lets the caller avoid recently-used words (see WordSearchHistoryStore) so the
     * same handful of words don't keep reappearing just because the pool is being sampled randomly
     * each time. If exclusion would leave fewer candidates than [wordCount] (e.g. the whole bank has
     * cycled through), it falls back to the full bank rather than failing to generate a puzzle.
     */
    fun generate(
        size: Int = 11,
        wordCount: Int = 8,
        random: Random = Random.Default,
        excludeWords: Set<String> = emptySet(),
    ): WordSearchPuzzle {
        val eligible = WORD_BANK.filter { it.length <= size }
        val afterExclusion = eligible.filter { it !in excludeWords }
        val pool = if (afterExclusion.size >= wordCount) afterExclusion else eligible

        val chosenWords = pool
            .shuffled(random)
            .take(wordCount)
            .sortedByDescending { it.length } // place longer words first - they're harder to fit later

        val grid = Array(size) { CharArray(size) { ' ' } }
        val placed = mutableListOf<PlacedWord>()

        for (word in chosenWords) {
            placeWord(word, grid, size, random)?.let { placed.add(it) }
        }

        for (row in 0 until size) {
            for (col in 0 until size) {
                if (grid[row][col] == ' ') {
                    grid[row][col] = 'A' + random.nextInt(26)
                }
            }
        }

        return WordSearchPuzzle(size, grid, placed)
    }

    private fun placeWord(word: String, grid: Array<CharArray>, size: Int, random: Random): PlacedWord? {
        repeat(200) {
            val (dRow, dCol) = DIRECTIONS.random(random)
            val startRowRange = validStartRange(size, dRow, word.length)
            val startColRange = validStartRange(size, dCol, word.length)
            if (startRowRange.isEmpty() || startColRange.isEmpty()) return@repeat

            val startRow = startRowRange.random(random)
            val startCol = startColRange.random(random)

            if (fits(word, grid, startRow, startCol, dRow, dCol, size)) {
                for (i in word.indices) {
                    grid[startRow + dRow * i][startCol + dCol * i] = word[i]
                }
                return PlacedWord(word, startRow, startCol, dRow, dCol)
            }
        }
        return null
    }

    private fun validStartRange(size: Int, delta: Int, length: Int): IntRange = when {
        delta == 0 -> 0 until size
        delta > 0 -> 0 until (size - (length - 1))
        else -> (length - 1) until size
    }

    private fun fits(
        word: String,
        grid: Array<CharArray>,
        startRow: Int,
        startCol: Int,
        dRow: Int,
        dCol: Int,
        size: Int,
    ): Boolean {
        for (i in word.indices) {
            val r = startRow + dRow * i
            val c = startCol + dCol * i
            if (r !in 0 until size || c !in 0 until size) return false
            val existing = grid[r][c]
            if (existing != ' ' && existing != word[i]) return false
        }
        return true
    }
}
