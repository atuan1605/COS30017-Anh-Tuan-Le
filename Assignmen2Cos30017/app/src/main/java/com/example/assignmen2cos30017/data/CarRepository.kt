package com.example.assignmen2cos30017.data

import com.example.assignmen2cos30017.R
import com.example.assignmen2cos30017.model.Car

object CarRepository {

    private val cars: MutableList<Car> = mutableListOf(
        Car(
            id = 1,
            name = "BMW 3 Series",
            model = "M Sport Edition",
            year = 2021,
            rating = 3.5f,
            kilometres = 32000,
            dailyCost = 75,
            imageResId = R.drawable.bmw,
            description = "The BMW 3 Series M Sport Edition delivers an exhilarating driving experience with its powerful inline-6 engine and sport-tuned suspension. Featuring adaptive M suspension, a sport differential, and aerodynamic M Sport bodywork, it blends daily usability with true performance DNA. The interior offers premium leather, ambient lighting, and the latest BMW iDrive system.",
            engine = "Inline-6 engine",
            seats = 5,
            transmission = "8-speed",
            fuel = "Gasoline",
            mileage = "14.70 kmpl",
            color = "Alpine White"
        ),
        Car(
            id = 2,
            name = "Mercedes-AMG G63",
            model = "AMG Night Package",
            year = 2024,
            rating = 5.0f,
            kilometres = 12000,
            dailyCost = 120,
            imageResId = R.drawable.g63,
            description = "The Mercedes-AMG G63 AMG Night Package is an icon of luxury and performance. Its handcrafted AMG 4.0L V8 biturbo engine produces 577hp, capable of 0–100 km/h in 4.5 seconds. The Night Package adds exclusive black chrome trim, a panoramic roof, and 22-inch AMG forged wheels, making every entrance an event.",
            engine = "V-8 biturbo",
            seats = 5,
            transmission = "9-speed",
            fuel = "Gasoline",
            mileage = "10.20 kmpl",
            color = "Obsidian Black"
        ),
        Car(
            id = 3,
            name = "Porsche 911",
            model = "Carrera 4S",
            year = 2022,
            rating = 4.0f,
            kilometres = 8500,
            dailyCost = 110,
            imageResId = R.drawable.porsche1,
            description = "The Porsche 911 Carrera 4S is the pinnacle of sports car engineering. With a rear-mounted 3.0L flat-six twin-turbo engine producing 450hp and all-wheel drive, it delivers razor-sharp handling in all conditions. The Porsche Active Suspension Management and rear-axle steering make it as agile on track as it is refined on road.",
            engine = "Flat-6 twin-turbo",
            seats = 4,
            transmission = "8-speed PDK",
            fuel = "Gasoline",
            mileage = "13.10 kmpl",
            color = "GT Silver Metallic"
        ),
        Car(
            id = 4,
            name = "Rolls-Royce Ghost",
            model = "Extended Wheelbase",
            year = 2023,
            rating = 4.9f,
            kilometres = 5000,
            dailyCost = 200,
            imageResId = R.drawable.rolls_royce,
            description = "The Rolls-Royce Ghost Extended Wheelbase redefines automotive luxury. Hand-built at the Home of Rolls-Royce in Goodwood, it offers an extra 170mm of rear passenger space, Starlight Headliner with 1,340 individually placed fibre-optic lights, and a Bespoke audio system. The 6.75L twin-turbocharged V12 glides in near silence.",
            engine = "V-12 twin-turbo",
            seats = 5,
            transmission = "8-speed",
            fuel = "Gasoline",
            mileage = "8.90 kmpl",
            color = "Kairos Orange"
        ),
        Car(
            id = 5,
            name = "Tesla Model S",
            model = "Plaid",
            year = 2025,
            rating = 4.7f,
            kilometres = 18000,
            dailyCost = 95,
            imageResId = R.drawable.tesla,
            description = "The Tesla Model S Plaid is the fastest production sedan ever made, with a tri-motor all-wheel drive system producing 1,020hp. It accelerates 0–100 km/h in under 2.1 seconds. The 17-inch landscape touchscreen, yoke steering, and over-the-air updates make it the most technologically advanced luxury vehicle on the road.",
            engine = "Tri-motor electric",
            seats = 5,
            transmission = "Single-speed",
            fuel = "Electric",
            mileage = "16.55 kmpl",
            color = "Gray Metallic"
        )
    )

    fun getAvailableCars(): MutableList<Car> = cars.filter { !it.isBooked }.toMutableList()

    fun getAllCars(): MutableList<Car> = cars.toMutableList()

    fun searchCars(query: String): List<Car> {
        val q = query.lowercase()
        return cars.filter {
            it.name.lowercase().contains(q) ||
            it.model.lowercase().contains(q) ||
            it.year.toString().contains(q)
        }
    }

    fun sortByRatingDesc(): List<Car> = cars.sortedByDescending { it.rating }

    fun sortByYearDesc(): List<Car> = cars.sortedByDescending { it.year }

    fun sortByCostAsc(): List<Car> = cars.sortedBy { it.dailyCost }

    fun bookCar(id: Int) {
        cars.find { it.id == id }?.isBooked = true
    }

    fun cancelCar(id: Int) {
        cars.find { it.id == id }?.isBooked = false
    }

    fun toggleFavourite(id: Int) {
        val car = cars.find { it.id == id } ?: return
        car.isFavourite = !car.isFavourite
    }

    fun getFavourites(): List<Car> = cars.filter { it.isFavourite }

    fun getBookedCars(): List<Car> = cars.filter { it.isBooked }

    fun getCarById(id: Int): Car? = cars.find { it.id == id }
}
