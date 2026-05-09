// port-lint: source ../starlark_syntax/src/syntax/grammar.lalrpop
package io.github.kotlinmania.starlark.syntax.parser

object Grammar {
    fun __action(state: Int, integer: Int): Int {
        return GrammarState.ACTION[state * 66 + integer].toInt()
    }

    fun __goto(state: Int, nt: Int): Int {
        
        return when (nt) {
            3 -> when (state) {
                0 -> 1
                91 -> 98
                5 -> 157
                20 -> 184
                else -> 269
            }
            8 -> 155
            11 -> 56
            14 -> 78
            17 -> 53
            20 -> 49
            23 -> 50
            26 -> 93
            31 -> when (state) {
                1 -> 19
                91 -> 99
                98 -> 103
                else -> 2
            }
            34 -> when (state) {
                10 -> 47
                else -> 3
            }
            50 -> when (state) {
                35 -> 200
                else -> 104
            }
            51 -> when (state) {
                56 -> 247
                else -> 201
            }
            53 -> 202
            54 -> when (state) {
                42 -> 212
                43 -> 213
                else -> 105
            }
            55 -> when (state) {
                80 -> 88
                81 -> 89
                11 -> 165
                14, 50 -> 173
                51 -> 240
                52 -> 241
                else -> 79
            }
            56 -> 61
            57 -> 106
            58 -> 107
            59 -> when (state) {
                33 -> 198
                else -> 108
            }
            60 -> when (state) {
                12, 49, 63 -> 167
                25 -> 190
                26 -> 191
                27 -> 192
                28 -> 193
                29 -> 194
                30 -> 195
                31 -> 196
                55 -> 246
                else -> 109
            }
            61 -> when (state) {
                32 -> 197
                else -> 110
            }
            62 -> 203
            63 -> 263
            64 -> 181
            65 -> 174
            66 -> 164
            67 -> when (state) {
                76 -> 288
                else -> 260
            }
            69 -> 76
            70 -> when (state) {
                94 -> 316
                101 -> 327
                else -> 83
            }
            71 -> when (state) {
                54 -> 244
                else -> 234
            }
            72 -> 111
            73 -> when (state) {
                78 -> 290
                else -> 264
            }
            75 -> 265
            76 -> 112
            77 -> 113
            78 -> 114
            79 -> 115
            80 -> when (state) {
                53 -> 242
                else -> 54
            }
            82 -> 293
            84 -> when (state) {
                49 -> 236
                else -> 168
            }
            85 -> when (state) {
                63 -> 262
                else -> 169
            }
            86 -> 116
            87 -> 117
            88 -> when (state) {
                8 -> 162
                9 -> 163
                18 -> 183
                38 -> 208
                39 -> 209
                40 -> 210
                41 -> 211
                else -> 118
            }
            89 -> when (state) {
                62, 76 -> 261
                else -> 62
            }
            90 -> 119
            91 -> 120
            92 -> 121
            93 -> 312
            94 -> when (state) {
                92 -> 313
                else -> 171
            }
            95 -> 122
            96 -> 123
            97 -> 170
            98 -> 124
            99 -> 125
            100 -> 126
            101 -> when (state) {
                50 -> 238
                else -> 175
            }
            103 -> 176
            104 -> 127
            105 -> 128
            106 -> 129
            107 -> 295
            109 -> when (state) {
                93 -> 101
                else -> 94
            }
            110 -> 130
            111 -> when (state) {
                15 -> 179
                21 -> 186
                else -> 131
            }
            112 -> 132
            114 -> when (state) {
                34 -> 199
                77 -> 289
                86 -> 306
                else -> 133
            }
            115 -> 134
            116 -> when (state) {
                22 -> 187
                23 -> 188
                else -> 135
            }
            117 -> 308
            118 -> when (state) {
                24 -> 189
                else -> 136
            }
            119 -> when (state) {
                66, 90, 100, 102 -> 270
                else -> 137
            }
            120 -> when (state) {
                44 -> 215
                60 -> 255
                else -> 4
            }
            121 -> 138
            122 -> when (state) {
                0, 1, 91, 98 -> 5
                else -> 20
            }
            123 -> when (state) {
                90 -> 311
                100 -> 326
                102 -> 330
                else -> 82
            }
            124 -> when (state) {
                10 -> 48
                3 -> 152
                7, 16, 61 -> 159
                13, 92 -> 172
                17, 53 -> 182
                36, 56 -> 204
                37 -> 207
                46, 97 -> 229
                47 -> 232
                57 -> 250
                58 -> 251
                59 -> 253
                65 -> 268
                67 -> 272
                68 -> 273
                70 -> 278
                71 -> 279
                72 -> 281
                73 -> 282
                74 -> 283
                75 -> 286
                84 -> 301
                85 -> 303
                95 -> 319
                96 -> 323
                else -> 139
            }
            126 -> when (state) {
                7 -> 160
                16 -> 180
                61 -> 257
                else -> 6
            }
            128 -> when (state) {
                79 -> 291
                88 -> 309
                89 -> 310
                else -> 45
            }
            129 -> when (state) {
                97 -> 324
                else -> 230
            }
            else -> 0
        }
        return 0
    }

    fun __expectedTokens(__state: Int): List<String> {
        return emptyList()
    }
}
