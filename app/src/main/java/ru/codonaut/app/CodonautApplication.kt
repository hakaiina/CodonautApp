package ru.codonaut.app

import android.app.Application
import ru.codonaut.app.di.AppContainer

// Application - это Android-компонент, который создаётся раньше Activity
// данный объект будет существовать на протяжении жизненного цикла процесса приложения
// мы размещаем AppContainer здесь, чтобы разные экраны и ViewModel могли получать одни и те же зависимости

class CodonautApplication : Application() {

    val container: AppContainer by lazy {
        AppContainer(this)
    }
}