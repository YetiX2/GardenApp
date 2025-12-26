package com.example.gardenapp.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Foundation
import androidx.compose.material.icons.outlined.House
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Yard
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.example.gardenapp.R
import com.example.gardenapp.data.db.GardenType
import com.example.gardenapp.data.db.ReferenceCultureEntity

internal val cultureIconMap = mapOf(
    "apple" to R.drawable.icon_culture_apple_generic,
    "pear" to R.drawable.icon_culture_pear_green_anjou,
    "cherry_sour" to R.drawable.icon_culture_cherry_sour_rainier,
    "cherry_sweet" to R.drawable.icon_culture_cherry_sweet_bing,
    "plum" to R.drawable.icon_culture_plum,
    "apricot" to R.drawable.icon_culture_apricot,
    "peach" to R.drawable.icon_culture_peach,
    //"quince" to R.drawable.icon_culture_quince,//Айва
    //"currant_black" to R.drawable.icon_culture_currant_black,//смородина чёрная
    //"currant_red" to R.drawable.icon_culture_currant_red,//смородина красная
    //"gooseberry" to R.drawable.icon_culture_gooseberry,//Крыжовник
    "raspberry" to R.drawable.icon_culture_raspberry,
    "blackberry" to R.drawable.icon_culture_blackberry,//Ежевика
    //"honeyberry" to R.drawable.icon_culture_honeyberry,//Жимолость съедобная
    "blueberry" to R.drawable.icon_culture_blueberry,//Голубика
    //"seabuckthorn" to R.drawable.icon_culture_seabuckthorn,//Облепиха
    "tomato" to R.drawable.icon_culture_tomato,
    "cucumber" to R.drawable.icon_culture_cucumber,
    "pepper_sweet" to R.drawable.icon_culture_pepper_sweet,
    "eggplant" to R.drawable.icon_culture_eggplant,
    "cabbage_white" to R.drawable.icon_culture_cabbage_green,
    "cabbage_broccoli" to R.drawable.icon_culture_cabbage_broccoli,
    "carrot" to R.drawable.icon_culture_carrot,
    "beet" to R.drawable.icon_culture_beet,
    "radish" to R.drawable.icon_culture_radish_cherry_bell,
    "onion" to R.drawable.icon_culture_onion_yellow,
    "garlic" to R.drawable.icon_culture_garlic,
    "potato" to R.drawable.icon_culture_potato_russet,
    "pumpkin_group" to R.drawable.icon_culture_pumpkin_group,
    //"legumes" to R.drawable.icon_culture_legumes,//Бобовые (горох/фасоль)
    "dill" to R.drawable.icon_culture_dill,
    "parsley" to R.drawable.icon_culture_parsley,
    "lettuce" to R.drawable.icon_culture_lettuce_bibb,
    "basil" to R.drawable.icon_culture_basil,
    "cilantro" to R.drawable.icon_culture_cilantro,
    "spinach" to R.drawable.icon_culture_spinach,
    "onion_green" to R.drawable.icon_culture_onion_green,
)

val ReferenceCultureEntity.icon: Painter
    @Composable
    get() = painterResource(id = cultureIconMap[this.id] ?: R.drawable.icon_culture_cilantro)

val GardenType.icon: ImageVector
    @Composable
    get() = when (this) {
        GardenType.PLOT -> Icons.Outlined.Map
        GardenType.GREENHOUSE -> Icons.Outlined.Foundation
        GardenType.BED -> Icons.Outlined.Yard
        GardenType.BUILDING -> Icons.Outlined.House
    }
