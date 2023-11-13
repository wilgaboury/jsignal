package com.github.wilgaboury.sigwig

import com.github.wilgaboury.jsignal.Provide
import com.github.wilgaboury.jsignal.ReactiveUtil

class Theme(builder: Builder) {
    val light: Int
    val dark: Int
    val primary: Int
    val secondary: Int
    val info: Int
    val success: Int
    val warning: Int
    val error: Int

    init {
        light = builder.light
        dark = builder.dark
        primary = builder.primary
        secondary = builder.secondary
        info = builder.info
        success = builder.success
        warning = builder.warning
        error = builder.error
    }

    fun toBuilder(): Builder {
        return builder()
                .setLight(light)
                .setDark(dark)
                .setPrimary(primary)
                .setSecondary(secondary)
                .setInfo(info)
                .setSuccess(success)
                .setWarning(warning)
                .setError(error)
    }

    class Builder {
        var light = EzColors.NEUTRAL_200
        var dark = EzColors.NEUTRAL_700
        var primary = EzColors.INDIGO_600
        var secondary = EzColors.PINK_500
        var info = EzColors.SKY_400
        var success = EzColors.EMERALD_400
        var warning = EzColors.AMBER_400
        var error = EzColors.RED_400
        fun setLight(light: Int): Builder {
            this.light = light
            return this
        }

        fun setDark(dark: Int): Builder {
            this.dark = dark
            return this
        }

        fun setPrimary(primary: Int): Builder {
            this.primary = primary
            return this
        }

        fun setSecondary(secondary: Int): Builder {
            this.secondary = secondary
            return this
        }

        fun setInfo(info: Int): Builder {
            this.info = info
            return this
        }

        fun setSuccess(success: Int): Builder {
            this.success = success
            return this
        }

        fun setWarning(warning: Int): Builder {
            this.warning = warning
            return this
        }

        fun setError(error: Int): Builder {
            this.error = error
            return this
        }

        fun build(): Theme {
            return Theme(this)
        }
    }

    companion object {
        val context = Provide.createContext(ReactiveUtil.createSignal(builder().build()))
        fun builder(): Builder {
            return Builder()
        }
    }
}
