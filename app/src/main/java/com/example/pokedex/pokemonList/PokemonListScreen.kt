package com.example.pokedex.pokemonList


import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.request.ImageRequest
import com.example.pokedex.R
import com.example.pokedex.data.models.PokedexListEntry
import com.example.pokedex.ui.theme.RobotoCondensed
import com.google.accompanist.coil.rememberCoilPainter
import java.util.*
import kotlin.random.Random


@Composable
fun PokemonListScreen(
    navController: NavController,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    // the surface lets us define how the background looks
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        // from top to bottom
        Column {
            Spacer(modifier = Modifier.height(20.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_international_pok_mon_logo),
                contentDescription = "Pokemon",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(CenterHorizontally)
            )
            SearchBar(
                hint = "Search...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // make call to viewModel and debounce the call
                // resets the list when empty
                viewModel.searchPokemonList(it)
            }
            Spacer(modifier = Modifier.height(16.dp))

            PokemonList(navController = navController)
        }
    }
}


@Composable
fun PokemonList(
    navController: NavController,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    val pokemonList by remember { viewModel.pokemonList }
    val endReached by remember { viewModel.endReached }
    val loadError by remember { viewModel.loadError }
    val isLoading by remember { viewModel.isLoading }
    val isSearching by remember {
        viewModel.isSearching
    }

    // like our recyclerView
    LazyColumn(contentPadding = PaddingValues(16.dp)) {

        // because we display 2 cards per row
        val itemCount = if (pokemonList.size % 2 == 0) {
            pokemonList.size / 2
        } else {
            pokemonList.size / 2 + 1
        }

        items(itemCount) {
            // this happens per every item like the inflater and binging

            // bottom of results but API stills has data and we are not searching
            if (it >= itemCount - 1 && !endReached && !isLoading && !isSearching) {
                viewModel.loadPokemonPaginated()
            }

            // our row of cards
            PokedexRow(rowIndex = it, entries = pokemonList, navController = navController)
        }
    }

    Box(
        contentAlignment = Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colors.primary)
        }
        if (loadError.isNotEmpty()) {
            RetrySection(error = loadError) {
                viewModel.loadPokemonPaginated()
            }
        }
    }

}


@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    hint: String = "",
    onSearch: (String) -> Unit = {}
) {

    // our states to manage the behaviour of our search bar
    var text by remember {
        mutableStateOf("")
    }
    var isHintDisplayed by remember {
        mutableStateOf(hint != "")
    }

    Box(modifier = modifier) {
        BasicTextField(
            value = text,
            onValueChange = {
                text = it
                onSearch(it)
            },
            maxLines = 1,
            singleLine = true,
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(5.dp, CircleShape)
                // this gives it the shape we want
                .background(Color.White, CircleShape)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .onFocusChanged {
                    isHintDisplayed = !it.isFocused
                }
        )
        // shows the hint
        if (isHintDisplayed) {
            Text(
                text = hint,
                color = Color.LightGray,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }
}

// represents every individual card from the pokemon list
@Composable
fun PokedexEntry(
    entry: PokedexListEntry,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: PokemonListViewModel = hiltViewModel()
) {

    val randomColor = Color(
        Random.nextFloat(),
        Random.nextFloat(),
        Random.nextFloat(),
    )

    // takes either dark or light theme
    val defaultDominantColor = randomColor

    var dominantColor by remember {
        mutableStateOf(defaultDominantColor)
    }

    // our main container
    Box(
        contentAlignment = Center,
        modifier = modifier
            .shadow(5.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .aspectRatio(1f)
            // makes the gradient look
            .background(
                Brush.verticalGradient(
                    listOf(
                        dominantColor,
                        defaultDominantColor
                    )
                )
            )
            .clickable {
                navController.navigate(
                    // the param we specified in the main activity
                    "pokemon_detail_screen/${dominantColor.toArgb()}/${entry.pokemonName}"
                )
            }
    ) {
        // from top to bottom
        Column {

            Image(
                painter = rememberCoilPainter(
                    request = ImageRequest.Builder(LocalContext.current)
                        .data(entry.imageUrl)
                        .target {
                            viewModel.calcDominantColor(it) { color ->
                                // this will trigger the recomposition of the
                                // composable
                                dominantColor = color
                            }
                        }.build()
                ), contentDescription = entry.pokemonName,
                modifier = Modifier
                    .size(120.dp)
                    .align(CenterHorizontally)
            )

            // An image loading library for Android backed by Kotlin Coroutines.
            /* CoilImage(
                 request = ImageRequest.Builder(LocalContext.current)
                     .data(entry.imageUrl)
                     .target {
                         viewModel.calcDominantColor(it) { color ->
                             // this will trigger the recomposition of the
                             // composable
                             dominantColor = color
                         }
                     }
                     .build(),
                 contentDescription = entry.pokemonName,
                 fadeIn = true,
                 modifier = Modifier
                     .size(120.dp)
                     .align(CenterHorizontally)
             ) {
                 // to show while loading the image
                 CircularProgressIndicator(
                     color = MaterialTheme.colors.primary,
                     modifier = Modifier.scale(0.5f)
                 )
             }*/

            Text(
                text = entry.pokemonName,
                fontFamily = RobotoCondensed,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PokedexRow(
    rowIndex: Int,
    entries: List<PokedexListEntry>,
    navController: NavController
) {
    // from top to bottom
    Column {
        // the cards from side to side
        Row {

            // shows the pokemons depending on the size of the entry
            PokedexEntry(
                // we receive the whole list but retrieve what we need
                entry = entries[rowIndex * 2],
                navController = navController,
                // the same amount of width per elements in the row
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            if (entries.size >= rowIndex * 2 + 2) {
                PokedexEntry(
                    entry = entries[rowIndex * 2 + 1],
                    navController = navController,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun RetrySection(
    error: String,
    onRetry: () -> Unit
) {
    Column {
        Text(error, color = Color.Red, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onRetry() },
            modifier = Modifier.align(CenterHorizontally)
        ) {
            Text(text = "Retry")
        }
    }
}
