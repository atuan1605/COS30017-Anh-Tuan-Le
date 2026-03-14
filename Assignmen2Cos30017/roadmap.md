# 🗺️ Car Rental App — Detailed Implementation Roadmap

> **Course:** COS30017 — Assignment 2  
> **Language:** Kotlin (Android)  
> **Min SDK:** 24 | **Target SDK:** 36  
> **Architecture:** Single-Activity + Detail Activity (no Fragments)

---

## 📋 Asset Inventory

**5 Car images confirmed in `drawable/`:**

| File | Assigned Car | Model |
|---|---|---|
| `bmw.jpg` | BMW 3 Series | M Sport Edition |
| `g63.webp` | Mercedes-AMG G63 | AMG Night Package |
| `porsche1.jpg` | Porsche 911 | Carrera 4S |
| `rolls_royce.webp` | Rolls-Royce Ghost | Extended Wheelbase |
| `tesla.webp` | Tesla Model S | Plaid |

---

## 🏗️ Phase 1 — Project Foundation & Dependencies

### 1.1 Update `libs.versions.toml`
- Add `RecyclerView` for Favourites horizontal list
- Add `CardView` for car card UI
- Add `ViewPager2` for one-car-at-a-time swipe in Popular section
- Confirm `Material 3` (already present) for `Slider`, `RatingBar`, `Snackbar`, `SearchView`, `PopupMenu`
- Add `kotlin-parcelize` plugin to `build.gradle.kts`

### 1.2 Update `build.gradle.kts`
- Apply `kotlin-parcelize` plugin
- Enable `viewBinding = true` under `buildFeatures`

### 1.3 Create package structure
```
com.example.assignmen2cos30017/
├── model/
│   └── Car.kt                    ← @Parcelize data class
├── data/
│   └── CarRepository.kt          ← In-memory singleton with 5 cars
├── adapter/
│   ├── CarPagerAdapter.kt        ← ViewPager2 adapter (Popular section)
│   └── FavouriteAdapter.kt       ← RecyclerView adapter (Favourites section)
├── ui/
│   ├── MainActivity.kt
│   └── CarDetailActivity.kt
└── utils/
    └── CreditManager.kt          ← Singleton managing 500-credit balance
```

---

## 📦 Phase 2 — Data Model

### 2.1 `Car.kt` — `@Parcelize` data class

```kotlin
@Parcelize
data class Car(
    val id: Int,
    val name: String,          // e.g. "BMW 3 Series"
    val model: String,         // e.g. "M Sport Edition"
    val year: Int,             // e.g. 2022
    val rating: Float,         // 1.0–5.0 (used in RatingBar)
    val kilometres: Int,       // e.g. 32000
    val dailyCost: Int,        // in credits
    val imageResId: Int,       // R.drawable.xxx
    val description: String,
    val engine: String,        // "V-8 engine"
    val seats: Int,            // 5
    val transmission: String,  // "8-speed"
    val fuel: String,          // "Gasoline"
    val mileage: String,       // "16.55 kmpl"
    val color: String,         // "Gray Metallic"
    var isBooked: Boolean = false,
    var isFavourite: Boolean = false
) : Parcelable
```

> **Why Parcelable?**  
> `Parcelable` is Android's optimised serialisation mechanism for IPC and Activity transitions.  
> `@Parcelize` auto-generates all boilerplate. Advantages over `Serializable`:  
> faster (no reflection), no extra temporary objects, recommended by Google.

### 2.2 `CarRepository.kt` — Object singleton
Methods:
- `getAvailableCars(): MutableList<Car>`
- `getAllCars(): MutableList<Car>`
- `searchCars(query: String): List<Car>`
- `sortByRatingDesc()`, `sortByYearDesc()`, `sortByCostAsc()`
- `bookCar(id: Int)`, `cancelCar(id: Int)`
- `toggleFavourite(id: Int)`
- `getFavourites(): List<Car>`

> **Note:** Sorting creates a temporary copy of the list — does NOT mutate the base data.

### 2.3 `CreditManager.kt` — Object singleton
```kotlin
object CreditManager {
    const val INITIAL_BALANCE = 500
    const val MAX_PER_RENTAL  = 400
    var balance = INITIAL_BALANCE

    fun canAfford(amount: Int): Boolean
    fun deduct(amount: Int)
    fun refund(amount: Int)
}
```

---

## 🎨 Phase 3 — Themes, Styles & Dark Mode Infrastructure

### 3.1 `themes.xml` (Light) & `values-night/themes.xml` (Dark)
Define semantic colour tokens:
- `colorPrimary`, `colorPrimaryVariant`
- `colorSurface`, `colorOnSurface`
- `colorBackground`, `colorOnBackground`
- `cardBackgroundColor`, `headerBackground`

Both `MainActivity` and `CarDetailActivity` inherit the same theme → Dark Mode adapts both screens automatically.

### 3.2 `styles.xml` — Reusable styles (≥2 used in ≥2 locations each)

| Style Name | Properties | Used In |
|---|---|---|
| `CarCardTextPrimary` | 16sp, bold, `colorOnSurface` | Car card + Detail title |
| `CarCardTextSecondary` | 13sp, `colorSecondary` | Car card meta + Detail specs |
| `SectionHeader` | 18sp, bold, letter-spacing | "Popular" + "Favourites" headers |
| `PriceTag` | 14sp, bold, `colorPrimary` | Car card price + Detail price |
| `SpecLabel` | 11sp, centre-aligned, `colorSecondary` | 3 spec icons in Detail screen |

### 3.3 `colors.xml`
Define brand palette: primary black/white for Light mode, inverted for Dark mode.

---

## 📱 Phase 4 — Main Activity

### 4.1 `activity_main.xml` Layout (matching mockup Frame 237983)

```
CoordinatorLayout
├── AppBarLayout
│   └── LinearLayout (header row)
│       ├── ImageButton  ← hamburger icon (left)
│       ├── TextView "Car Rental" (center, weight=1)
│       └── MaterialButton "Dark Mode" toggle (right)
│
└── NestedScrollView
    ├── TextView "Hello, Anh Tuan"
    ├── TextView "Balance: XXX credits"      ← updated dynamically
    │
    ├── LinearLayout (search row)
    │   ├── SearchView (weight=1)
    │   └── ImageButton (sort/filter icon)
    │
    ├── TextView "Popular" (SectionHeader style)
    ├── ViewPager2 (height=wrap, horizontal, clip siblings false)
    │   └── [item_car_card.xml per page]
    │       ├── CardView
    │       │   ├── ImageView (car photo)
    │       │   └── ImageButton ♡ (top-right overlay, favourite)
    │       ├── TextView name + PriceTag
    │       ├── TextView Model / Year / Km (SecondaryText style)
    │       ├── RatingBar (non-TextView ✅ Requirement)
    │       └── LinearLayout (horizontal buttons)
    │           ├── MaterialButton "Rent" (weight=1)
    │           └── MaterialButton "Next" (weight=1)
    ├── LinearLayout (dot indicators)
    │
    ├── TextView "Favourites" (SectionHeader style, GONE if empty)
    └── RecyclerView (horizontal, GONE if empty)
        └── [item_favourite_card.xml per item]
```

### 4.2 `MainActivity.kt` Logic

**Initialisation:**
- Set up `ViewPager2` with `CarPagerAdapter`
- Apply `ZoomOutPageTransformer` animation to ViewPager2
- Set up dot indicators synced to ViewPager2 page changes
- Set up `RecyclerView` with `FavouriteAdapter` (horizontal `LinearLayoutManager`)
- Display initial balance

**SearchView:**
```
setOnQueryTextListener {
    onQueryTextChange → CarRepository.searchCars(query) → adapter.updateList()
    onQueryTextSubmit → same
    empty text       → restore full list
}
```

**Sort PopupMenu (anchored to filter icon):**
```
"Rating: high to low"   → sortByRatingDesc()
"Year: newest to oldest" → sortByYearDesc()
"Cost: low to high"     → sortByCostAsc()
→ adapter.updateList(sortedCopy)  // does NOT change base data
```

**Dark Mode Toggle:**
```kotlin
if (AppCompatDelegate.getDefaultNightMode() == MODE_NIGHT_YES)
    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
else
    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
```

**Rent Button → navigate to Detail:**
```kotlin
val intent = Intent(this, CarDetailActivity::class.java)
intent.putExtra("car", car)   // Car is Parcelable
startActivityForResult(intent, REQUEST_RENT, animationBundle)
```

**Next Button:**
```kotlin
// Progress through cars one by one in the ViewPager
if (viewPager.currentItem < adapter.itemCount - 1) {
    viewPager.currentItem += 1
} else {
    viewPager.currentItem = 0 // loop back or show toast
}
```

**`onActivityResult`:**
- `RESULT_OK`   → update car state in Repository, refresh adapters, animate balance
- `RESULT_CANCELED` → refresh adapters (car re-available)

**Favourite toggle (from adapter callback):**
- `CarRepository.toggleFavourite(id)`
- Heart pulse animation
- Refresh Favourites RecyclerView (show/hide section)

---

## 📄 Phase 5 — Car Detail Activity

### 5.1 `activity_car_detail.xml` Layout (matching mockup Frame 237984)

```
CoordinatorLayout
├── AppBarLayout
│   └── CollapsingToolbarLayout
│       ├── ImageView (hero car photo, height=280dp)
│       │   └── shared element transition name = "car_image_[id]"
│       └── Toolbar (back arrow)
│           └── ImageButton ♡ (top-right in toolbar)
│
└── NestedScrollView
    ├── CardView (bottom sheet style, negative top margin overlapping hero)
    │   ├── TextView car name (CarCardTextPrimary style)
    │   ├── RatingBar (display-only, non-TextView ✅)
    │   │
    │   ├── Row: engine icon | seats icon | color icon (with labels)
    │   │
    │   ├── 3-column spec grid
    │   │   ├── [icon] Mileage   → value
    │   │   ├── [icon] Transmission → value
    │   │   └── [icon] Fuel      → value
    │   │
    │   ├── TextView "Description" (SectionHeader style)
    │   ├── TextView description body (CarCardTextSecondary style)
    │   │
    │   ├── Divider
    │   ├── TextView "Rent Price: X credits/day" (PriceTag style)
    │   │
    │   ├── TextInputLayout + TextInputEditText "Name (Required)"
    │   ├── TextInputLayout + TextInputEditText "Phone (Required)"
    │   │
    │   ├── TextView "Select rental duration (days):"
    │   ├── Slider (valueFrom=1, valueTo=7, stepSize=1) ← non-TextView ✅
    │   ├── TextView "X day(s)" (updates live)
    │   ├── TextView "Total: X credits" (updates live, red if over limit)
    │   ├── TextView error message (GONE by default, red)
    │   │
    │   └── MaterialButton "Save" / "Cancel Booking"
```

### 5.2 `CarDetailActivity.kt` Logic

**Receive data:**
```kotlin
val car = intent.getParcelableExtra<Car>("car")
```

**Slider listener:**
```kotlin
slider.addOnChangeListener { _, value, _ ->
    val days = value.toInt()
    val total = car.dailyCost * days
    tvDays.text = "$days day(s)"
    tvTotal.text = "Total: $total credits"
    validateTotal(total)
}
```

**Validation:**
```kotlin
fun validateTotal(total: Int) {
    when {
        total > CreditManager.MAX_PER_RENTAL →
            showError("Exceeds max rental of 400 credits")
        total > CreditManager.balance →
            showError("Insufficient balance (${CreditManager.balance} credits remaining)")
        else → clearError(), enableSave()
    }
}
```

**Save button (Confirm Booking):**
```kotlin
// Deduct credits, mark car as booked
CreditManager.deduct(total)
CarRepository.bookCar(car.id)
car.isBooked = true
Snackbar.make(root, "Booking confirmed! -$total credits", Snackbar.LENGTH_LONG).show()
val result = Intent().putExtra("car", car).putExtra("totalCost", total)
setResult(RESULT_OK, result)
finish()
```

**Back / Cancel button:**
```kotlin
// If car was booked in this session, refund
if (wasBookedInThisSession) {
    CreditManager.refund(totalCost)
    CarRepository.cancelCar(car.id)
    car.isBooked = false
    Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show()
}
setResult(RESULT_CANCELED)
finish()
```

---

## ⭐ Phase 6 — Favourites Feature

| Action | Trigger | Result |
|---|---|---|
| Toggle favourite | Tap heart button | `CarRepository.toggleFavourite(id)`, heart pulse animation |
| Toggle favourite | Long-press car image | Same as above |
| Favourites section | On main screen | RecyclerView + header appear when list non-empty, hide when empty |
| Favourite in detail | Tap heart in toolbar | Updates repository, result passed back on Save |
| Visual state | Filled ♥ vs outline ♡ | `ic_favorite` vs `ic_favorite_border` Material icons |

---

## ✨ Phase 7 — Animations (All Required)

### 7.1 Activity Transitions
| Animation | File | Usage |
|---|---|---|
| Slide up (enter) | `res/anim/slide_in_up.xml` | CarDetailActivity enter |
| Slide down (exit) | `res/anim/slide_out_down.xml` | CarDetailActivity exit |
| Fade in | `res/anim/fade_in.xml` | Return to MainActivity |
| Shared element | Built-in API | Car image card → detail hero |

### 7.2 In-screen Animations
| Animation | Mechanism | Trigger |
|---|---|---|
| Heart pulse | `ObjectAnimator` scaleX/Y 1.0→1.4→1.0, 300ms | Favourite toggled |
| Balance counter | `ValueAnimator` old→new value, 600ms | After booking/cancel |
| ViewPager2 transform | `ZoomOutPageTransformer` (custom) | Swipe between cars |
| RecyclerView fade-in | `alpha 0→1` on `onViewAttachedToWindow` | Favourites list item appears |
| Card scale on press | `StateListAnimator` scale 0.97 on pressed | Touch any car card |
| Save button feedback | Ripple + `AnimatorSet` pulse | Save clicked |
| Search bar expand | `ValueAnimator` width + `alpha` | SearchView focused |
| Error shake | `TranslateAnimator` horizontal shake | Validation fails (e.g., empty name) |

### 7.3 Animation Files to Create
```
res/
├── anim/
│   ├── slide_in_up.xml
│   ├── slide_out_down.xml
│   ├── fade_in.xml
│   ├── fade_out.xml
│   └── item_fade_in.xml
└── animator/
    ├── heart_pulse.xml
    ├── card_press.xml
    └── error_shake.xml
```

---

## 🧪 Phase 8 — Error Handling & Validation

| Scenario | UI Response | Blocks navigation? |
|---|---|---|
| Name is empty | Red error on TextInputLayout ("Required") | ✅ Yes — Save disabled |
| Phone lacks digits | Red error on TextInputLayout ("Invalid phone") | ✅ Yes — Save disabled |
| Total cost > 400 credits | Red error text below total | ✅ Yes — Save disabled |
| Insufficient balance | Red error text below total | ✅ Yes — Save disabled |
| Car already booked | Button label = "Cancel Booking" | — |
| Search returns 0 results | Empty state TextView shown | — |
| Slider at minimum (1 day) | Normal, valid state | — |

---

## 📁 Phase 9 — Complete File Checklist

```
app/src/main/
├── AndroidManifest.xml              (add CarDetailActivity)
├── java/com/example/assignmen2cos30017/
│   ├── model/
│   │   └── Car.kt
│   ├── data/
│   │   └── CarRepository.kt
│   ├── adapter/
│   │   ├── CarPagerAdapter.kt
│   │   └── FavouriteAdapter.kt
│   ├── ui/
│   │   ├── MainActivity.kt
│   │   └── CarDetailActivity.kt
│   └── utils/
│       └── CreditManager.kt
└── res/
    ├── anim/
    │   ├── slide_in_up.xml
    │   ├── slide_out_down.xml
    │   ├── fade_in.xml
    │   ├── fade_out.xml
    │   └── item_fade_in.xml
    ├── animator/
    │   ├── heart_pulse.xml
    │   ├── card_press.xml
    │   └── error_shake.xml
    ├── layout/
    │   ├── activity_main.xml
    │   ├── activity_car_detail.xml
    │   ├── item_car_card.xml
    │   └── item_favourite_card.xml
    ├── values/
    │   ├── colors.xml
    │   ├── strings.xml
    │   ├── styles.xml               ← 4+ reusable styles
    │   └── themes.xml               ← Light theme
    ├── values-night/
    │   └── themes.xml               ← Dark theme
    └── drawable/
        ├── bmw.jpg          ✅
        ├── g63.webp         ✅
        ├── porsche1.jpg     ✅
        ├── rolls_royce.webp ✅
        └── tesla.webp       ✅
```

---

## 🔢 Implementation Order

```
Phase 1  →  build.gradle + libs.versions.toml + package structure
Phase 2  →  Car.kt + CarRepository.kt + CreditManager.kt
Phase 3  →  themes.xml (Light + Dark) + styles.xml + colors.xml
Phase 4a →  activity_main.xml + item_car_card.xml + item_favourite_card.xml
Phase 4b →  CarPagerAdapter.kt + FavouriteAdapter.kt
Phase 4c →  MainActivity.kt (ViewPager2, search, sort, dark mode, balance)
Phase 5a →  activity_car_detail.xml
Phase 5b →  CarDetailActivity.kt (Parcelable receive, Slider, Checkout, Cancel)
Phase 6  →  Favourites toggle + long-press + section show/hide
Phase 7  →  All animation XML files + wire into activities/adapters
Phase 8  →  Error validation polish + edge cases
Phase 9  →  AndroidManifest.xml update + final review
```

---

## 📝 Report Checklist (Assignment Requirements)

- [ ] Explain `Parcelable` components and advantages over `Serializable`
- [ ] Two user stories and use cases
- [ ] Two UI layout sketches with widget justification
- [ ] Design choices discussion
- [ ] Challenges, explorations, and takeaways
- [ ] At least 2 non-TextView widgets (`RatingBar`, `Slider`)
- [ ] At least 2 styles used in 2+ locations
- [ ] Dark Mode applies to at least 2 screens
- [ ] Toast/Snackbar for booking confirmed/cancelled
- [ ] Credit limit enforced (max 400 per rental, max 500 total)

---

*Last updated: March 14, 2026*
