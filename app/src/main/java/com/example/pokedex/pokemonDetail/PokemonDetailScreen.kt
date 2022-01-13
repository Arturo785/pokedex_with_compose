package com.example.pokedex.pokemonDetail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pokedex.R
import com.example.pokedex.data.remote.responses.PokemonResponse
import com.example.pokedex.data.remote.responses.Type
import com.example.pokedex.util.Resource
import com.example.pokedex.util.parseStatToAbbr
import com.example.pokedex.util.parseStatToColor
import com.example.pokedex.util.parseTypeToColor
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.imageloading.rememberDrawablePainter
import java.lang.Math.round
import java.util.*


@Composable
fun PokemonDetailScreen(
    dominantColor: Color,
    pokemonName: String,
    navController: NavController,
    topPadding: Dp = 20.dp,
    pokemonImageSize: Dp = 200.dp,
    viewModel: PokemonDetailViewModel = hiltViewModel()
) {

    val pokemonInfo = produceState<Resource<PokemonResponse>>(initialValue = Resource.Loading()) {
        // this is the producer meaning it will take this value when finish loading
        value = viewModel.getPokemonInfo(pokemonName)
    }.value

    // our main container and kind of like the canvas
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dominantColor)
            .padding(bottom = 16.dp)
    ) {

        PokemonDetailTopSection(
            navController = navController,
            modifier = Modifier
                .fillMaxWidth()
                // to only fill the 20% of the screen
                .fillMaxHeight(0.2f)
                .align(Alignment.TopCenter)
        )


        PokemonDetailStateWrapper(
            pokemonInfo = pokemonInfo,

            // this one gives the box form where the info is displayed in
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = topPadding + pokemonImageSize / 2f,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
                .shadow(10.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colors.surface)
                .padding(16.dp)
                .align(Alignment.BottomCenter),

            loadingModifier = Modifier
                .size(100.dp)
                .align(Alignment.Center)
                .padding(
                    top = topPadding + pokemonImageSize / 2f,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
        )

        // goes to the bottom because as the box works it
        // overlays layer from layer and the last component goes
        // in top of everything
        // the image with offset to go out of boundaries if available
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (pokemonInfo is Resource.Success) {

                pokemonInfo.data?.sprites?.let {
                    Image(
                        painter = rememberCoilPainter(it.frontDefault),
                        contentDescription = pokemonInfo.data.name,
                        modifier = Modifier
                            .size(pokemonImageSize)
                            .offset(y = topPadding)
                    )
                }
            }
        }

    }

}


// the part that has the gradient and the arrow button
@Composable
fun PokemonDetailTopSection(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            // our gradient
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black,
                        Color.Transparent
                    )
                )
            )
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "back arrow",
            tint = Color.White,
            modifier = Modifier
                .size(36.dp)
                // on x and y axis
                .offset(16.dp, 16.dp)
                .clickable {
                    navController.popBackStack()
                }
        )
    }
}

@Composable
fun PokemonDetailStateWrapper(
    pokemonInfo: Resource<PokemonResponse>,
    // we pass modifiers in order to control how it looks from the
    // calling part
    modifier: Modifier = Modifier,
    loadingModifier: Modifier = Modifier
) {
    when (pokemonInfo) {
        is Resource.Success -> {
            PokemonDetailSection(
                pokemonInfo = pokemonInfo.data!!,
                modifier = modifier
                    .offset(y = (-20).dp)
            )
        }
        is Resource.Error -> {
            Text(
                text = pokemonInfo.message ?: "something went wrong",
                color = Color.Red,
                modifier = modifier
            )
        }
        is Resource.Loading -> {
            CircularProgressIndicator(
                color = MaterialTheme.colors.primary,
                modifier = loadingModifier
            )
        }
    }
}

@Composable
fun PokemonDetailSection(
    pokemonInfo: PokemonResponse,
    modifier: Modifier = Modifier
) {
    // in case of smaller device
    val scrollState = rememberScrollState()

    // from top to bottom
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .offset(y = 100.dp)
            // in case of smaller device
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "#${pokemonInfo.id} ${pokemonInfo.name.capitalize(Locale.ROOT)}",
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onSurface
        )
        PokemonTypeSection(types = pokemonInfo.types)

        PokemonDetailDataSection(
            pokemonWeight = pokemonInfo.weight,
            pokemonHeight = pokemonInfo.height
        )

        PokemonBaseStats(pokemonInfo = pokemonInfo)

    }
}

@Composable
fun PokemonTypeSection(types: List<Type>) {
    // align it side to side
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(16.dp)
    ) {
        for (type in types) {
            // the container of each type
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .clip(CircleShape)
                    .background(parseTypeToColor(type))
                    .height(35.dp)
            ) {
                Text(
                    text = type.type.name.capitalize(Locale.ROOT),
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun PokemonDetailDataSection(
    pokemonWeight: Int,
    pokemonHeight: Int,
    sectionHeight: Dp = 80.dp
) {

    // we use remember to avoid recalculation on every recomposition
    val pokemonWeightInKg = remember {
        round(pokemonWeight * 100f) / 1000f
    }
    val pokemonHeightInMeters = remember {
        round(pokemonHeight * 100f) / 1000f
    }
    // from side to side
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        PokemonDetailDataItem(
            dataValue = pokemonWeightInKg,
            dataUnit = "kg",
            dataIcon = painterResource(id = R.drawable.ic_weight),
            // makes the box even
            modifier = Modifier.weight(1f)
        )
        Spacer(
            modifier = Modifier
                .size(1.dp, sectionHeight)
                .background(Color.LightGray)
        )
        PokemonDetailDataItem(
            dataValue = pokemonHeightInMeters,
            dataUnit = "m",
            dataIcon = painterResource(id = R.drawable.ic_height),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PokemonDetailDataItem(
    dataValue: Float,
    dataUnit: String,
    dataIcon: Painter,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Icon(painter = dataIcon, contentDescription = null, tint = MaterialTheme.colors.onSurface)
        // the little bar that separates the items
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$dataValue$dataUnit",
            color = MaterialTheme.colors.onSurface
        )
    }
}

@Composable
fun PokemonStat(
    statName: String,
    statValue: Int,
    statMaxValue: Int,
    statColor: Color,
    height: Dp = 28.dp,
    animDuration: Int = 1000,
    animDelay: Int = 0
) {

    var animationPlayed by remember {
        mutableStateOf(false)
    }

    // with this whenever the value changes the recomposition happens smoothly and animated
    // instead of instantly

    // starts as 0, then the animationPlayed value changes, recomposes and now goes from 0 to the calculated
    // value
    val curPercent = animateFloatAsState(
        targetValue = if (animationPlayed) {
            // gives us the max length to animate to
            statValue / statMaxValue.toFloat()
        } else 0f,

        // additional modifiers
        animationSpec = tween(
            animDuration,
            animDelay
        )
    )

    //We can use LaunchedEffect to perform actions which are tied to the lifecycle of the composable

    //LaunchedEffect should be used when you want that some action must be taken when your composable is first launched

    //https://stackoverflow.com/questions/66474049/using-remembercoroutinescope-vs-launchedeffect
    //https://proandroiddev.com/jetpack-compose-side-effects-launchedeffect-59d2330d7834

    // has the value of true because the effect is launched when the key changes
    // if a constant passed, it never triggers again
    LaunchedEffect(key1 = true) {
        // the value gets set after a successfull composition
        animationPlayed = true
    }

    // the container shadow box
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(
                if (isSystemInDarkTheme()) {
                    Color(0xFF505050)
                } else {
                    Color.LightGray
                }
            )
    ) {
        // to display the values in row orientation
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                // takes the parent height
                .fillMaxHeight()
                // only takes the space calculated
                .fillMaxWidth(curPercent.value)
                .clip(CircleShape)
                .background(statColor)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = statName,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = (curPercent.value * statMaxValue).toInt().toString(),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PokemonBaseStats(
    pokemonInfo: PokemonResponse,
    animDelayPerItem: Int = 100
) {
    // this defines the bigger row to fill
    // in order to make the rest
    val maxBaseStat = remember {
        pokemonInfo.stats.maxOf { it.baseStat }
    }

    // one per one from top to bottom
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Base stats:",
            fontSize = 20.sp,
            color = MaterialTheme.colors.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        // all stats in the list
        for (i in pokemonInfo.stats.indices) {
            val stat = pokemonInfo.stats[i]

            PokemonStat(
                statName = parseStatToAbbr(stat),
                statValue = stat.baseStat,
                statMaxValue = maxBaseStat,
                statColor = parseStatToColor(stat),
                animDelay = i * animDelayPerItem // greater per last item
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
