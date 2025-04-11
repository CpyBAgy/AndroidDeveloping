# Лабораторная работа №2: Разработка приложения с открытым API

## 📌 Цель работы  
Создать Android-приложение, использующее одно из предложенных **открытых API**, реализуя **Clean Architecture**, **RecyclerView**, **пагинацию** и **Dependency Injection (Dagger / Hilt)**.  

## 🔥 Требования к функционалу  
1. Выбрать одно из предложенных API и интегрировать его в приложение.  
2. Реализовать **архитектуру Clean Architecture** (разделение на data, domain, presentation).  
3. Использовать **RecyclerView** для отображения списка данных.  
4. **Добавить пагинацию**, чтобы подгружать данные частями.  
5. Использовать **Dagger или Hilt** для внедрения зависимостей.  

## 🎯 Доступные API (на выбор)  
- 🎬 [**TheMovieDB API**](https://www.themoviedb.org/documentation/api) – каталог фильмов и сериалов.  
- ⛅ [**OpenWeather API**](https://openweathermap.org/api) – погода в реальном времени.  
- 📰 [**NewsAPI**](https://newsapi.org/) – новости из мировых СМИ.  
- 🎮 [**PokeAPI**](https://pokeapi.co/) – база данных покемонов.  
- 🎵 [**Spotify API**](https://developer.spotify.com/documentation/web-api/) – каталог музыки и подкастов.  

## 💡 Рекомендации по реализации  
1. **Проект должен быть разделен на модули:**  
   - `data` – работа с API и базой данных.  
   - `domain` – бизнес-логика и use-case'ы.  
   - `presentation` – UI и ViewModel.  
2. **Для запросов к API использовать Retrofit.**  
3. **Для управления потоками использовать Coroutines.**  
4. **Пагинацию реализовать с Paging 3 (желательно, но можно и вручную).**  
5. **Использовать Dagger или Hilt для DI.**  

---

## 📚 Справка  

### 🔗 1. Как работать с Retrofit  

**Добавляем в `build.gradle.kts`:**  
```kotlin
dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}
```
**Создаем API-интерфейс:**  

```kotlin
interface MovieApi {
    @GET("movie/popular")
    suspend fun getPopularMovies(@Query("page") page: Int): MovieResponse
}
```

**Настраиваем Retrofit**
```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.themoviedb.org/3/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val api = retrofit.create(MovieApi::class.java)
```

## ⚡ 2. Как использовать Coroutines для запросов
Добавляем Coroutines в зависимости:  

```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
}
```
**Запускаем запрос в ViewModel:**  

```kotlin
class MovieViewModel(private val api: MovieApi) : ViewModel() {
    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>> = _movies

    fun loadMovies(page: Int) {
        viewModelScope.launch {
            try {
                val response = api.getPopularMovies(page)
                _movies.postValue(response.results)
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error loading movies", e)
            }
        }
    }
}
```

## ⚡ 3. Можно использовать Hilt вместо Dagger

```kotlin
@HiltAndroidApp
class MyApplication : Application()

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    fun provideMovieApi(retrofit: Retrofit): MovieApi =
        retrofit.create(MovieApi::class.java)
}

@HiltViewModel
class MovieViewModel @Inject constructor(private val api: MovieApi) : ViewModel() {
    fun loadMovies() { /* запрос в API */ }
}
```

