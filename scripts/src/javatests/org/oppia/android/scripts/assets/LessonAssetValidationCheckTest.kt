package org.oppia.android.scripts.assets

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import javax.imageio.ImageIO

/**
 * Tests for [LessonAssetValidationCheck].
 *
 * Note on HTML encoding: The exploration JSON files use double-encoded HTML entities. In the JSON
 * file on disk, the attribute values use JSON string escaping (\" for quotes) and HTML entity
 * encoding (&amp;quot; for inner quotes). When the JSON is parsed, \" becomes " and &amp;quot;
 * remains as-is. The script's regex captures the attribute value and then decodes &amp;quot; → ".
 *
 * Real format example (from GJ2rLXRKD5hw_1.json):
 *   math_content-with-value=\"{&amp;quot;raw_latex&amp;quot;:&amp;quot;y=mx+b&amp;quot;}\"
 *   filepath-with-value=\"&amp;quot;img_xxx.png&amp;quot;\"
 */
class LessonAssetValidationCheckTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private val originalOut: PrintStream = System.out
  private lateinit var outContent: ByteArrayOutputStream
  private lateinit var assetsDir: File

  @Before
  fun setUp() {
    outContent = ByteArrayOutputStream()
    System.setOut(PrintStream(outContent))
    assetsDir = tempFolder.newFolder("assets")
  }

  @After
  fun restoreStreams() {
    System.setOut(originalOut)
  }

  @Test
  fun testNoAssets_passes() {
    val checker = LessonAssetValidationCheck(assetsDir)
    val hasPassed = checker.execute()

    assertThat(hasPassed).isTrue()
    assertThat(outContent.toString()).contains("No exploration files found")
  }

  @Test
  fun testMissingAssetsDirectory_passes() {
    val nonExistentDir = File(tempFolder.root, "non_existent")
    val checker = LessonAssetValidationCheck(nonExistentDir)
    val hasPassed = checker.execute()

    assertThat(hasPassed).isTrue()
  }

  @Test
  fun testExplorationWithValidMathLatex_passes() {
    // Math JSON is directly inside the attribute: {&amp;quot;raw_latex&amp;quot;:&amp;quot;...&amp;quot;}
    val explorationJson =
      """
      {
        "exploration_id": "test_exploration",
        "exploration": {
          "init_state_name": "Introduction",
          "states": {
            "Introduction": {
              "content": {
                "content_id": "content",
                "html": "<oppia-noninteractive-math math_content-with-value=\"{&amp;quot;raw_latex&amp;quot;:&amp;quot;\\\\frac{1}{2}&amp;quot;}\"></oppia-noninteractive-math>"
              },
              "interaction": {
                "answer_groups": [],
                "default_outcome": null,
                "hints": [],
                "solution": null
              }
            }
          }
        }
      }
    """.trimIndent()

    File(assetsDir, "test_exploration.json").writeText(explorationJson)

    val checker = LessonAssetValidationCheck(assetsDir)
    val hasPassed = checker.execute()

    assertThat(hasPassed).isTrue()
  }

  @Test
  fun testExplorationWithEmptyRawLatex_fails() {
    // Empty raw_latex with svg_filename present → should warn about SVG fallback.
    val explorationJson =
      """
      {
        "exploration_id": "test_exploration",
        "exploration": {
          "init_state_name": "Introduction",
          "states": {
            "Introduction": {
              "content": {
                "content_id": "content",
                "html": "<oppia-noninteractive-math math_content-with-value=\"{&amp;quot;raw_latex&amp;quot;:&amp;quot;&amp;quot;,&amp;quot;svg_filename&amp;quot;:&amp;quot;math.svg&amp;quot;}\"></oppia-noninteractive-math>"
              },
              "interaction": {
                "answer_groups": [],
                "default_outcome": null,
                "hints": [],
                "solution": null
              }
            }
          }
        }
      }
    """.trimIndent()

    File(assetsDir, "test_exploration.json").writeText(explorationJson)

    val checker = LessonAssetValidationCheck(assetsDir)
    val hasPassed = checker.execute()

    assertThat(hasPassed).isFalse()
    assertThat(outContent.toString()).contains("SVG fallback")
  }

  @Test
  fun testExplorationWithNoLatexAndNoSvg_fails() {
    // Empty math JSON → no LaTeX and no SVG.
    val explorationJson =
      """
      {
        "exploration_id": "test_exploration",
        "exploration": {
          "init_state_name": "Introduction",
          "states": {
            "Introduction": {
              "content": {
                "content_id": "content",
                "html": "<oppia-noninteractive-math math_content-with-value=\"{}\"></oppia-noninteractive-math>"
              },
              "interaction": {
                "answer_groups": [],
                "default_outcome": null,
                "hints": [],
                "solution": null
              }
            }
          }
        }
      }
    """.trimIndent()

    File(assetsDir, "test_exploration.json").writeText(explorationJson)

    val checker = LessonAssetValidationCheck(assetsDir)
    val hasPassed = checker.execute()

    assertThat(hasPassed).isFalse()
    assertThat(outContent.toString()).contains("no LaTeX content")
  }

  @Test
  fun testExplorationWithMathInAnswerGroupFeedback_fails() {
    // Math tag in answer group feedback with no LaTeX.
    val explorationJson =
      """
      {
        "exploration_id": "test_exploration",
        "exploration": {
          "init_state_name": "Introduction",
          "states": {
            "Introduction": {
              "content": {
                "content_id": "content",
                "html": "<p>Question</p>"
              },
              "interaction": {
                "answer_groups": [
                  {
                    "outcome": {
                      "feedback": {
                        "content_id": "feedback_1",
                        "html": "<oppia-noninteractive-math math_content-with-value=\"{}\"></oppia-noninteractive-math>"
                      }
                    }
                  }
                ],
                "default_outcome": null,
                "hints": [],
                "solution": null
              }
            }
          }
        }
      }
    """.trimIndent()

    File(assetsDir, "test_exploration.json").writeText(explorationJson)

    val checker = LessonAssetValidationCheck(assetsDir)
    val hasPassed = checker.execute()

    assertThat(hasPassed).isFalse()
  }

  @Test
  fun testExplorationWithMathInDefaultOutcome_fails() {
    // Math tag in default_outcome feedback with no LaTeX.
    val explorationJson =
      """
      {
        "exploration_id": "test_exploration",
        "exploration": {
          "init_state_name": "Introduction",
          "states": {
            "Introduction": {
              "content": {
                "content_id": "content",
                "html": "<p>Question</p>"
              },
              "interaction": {
                "answer_groups": [],
                "default_outcome": {
                  "dest": "Introduction",
                  "feedback": {
                    "content_id": "default_outcome",
                    "html": "<oppia-noninteractive-math math_content-with-value=\"{}\"></oppia-noninteractive-math>"
                  }
                },
                "hints": [],
                "solution": null
              }
            }
          }
        }
      }
    """.trimIndent()

    File(assetsDir, "test_exploration.json").writeText(explorationJson)

    val checker = LessonAssetValidationCheck(assetsDir)
    val hasPassed = checker.execute()

    assertThat(hasPassed).isFalse()
    assertThat(outContent.toString()).contains("no LaTeX content")
  }

  @Test
  fun testExplorationWithMathInHint_fails() {
    // Math tag in hint with no LaTeX.
    val explorationJson =
      """
      {
        "exploration_id": "test_exploration",
        "exploration": {
          "init_state_name": "Introduction",
          "states": {
            "Introduction": {
              "content": {
                "content_id": "content",
                "html": "<p>Question</p>"
              },
              "interaction": {
                "answer_groups": [],
                "default_outcome": null,
                "hints": [
                  {
                    "hint_content": {
                      "content_id": "hint_1",
                      "html": "<oppia-noninteractive-math math_content-with-value=\"{&amp;quot;svg_filename&amp;quot;:&amp;quot;hint_eq.svg&amp;quot;}\"></oppia-noninteractive-math>"
                    }
                  }
                ],
                "solution": null
              }
            }
          }
        }
      }
    """.trimIndent()

    File(assetsDir, "test_exploration.json").writeText(explorationJson)

    val checker = LessonAssetValidationCheck(assetsDir)
    val hasPassed = checker.execute()

    assertThat(hasPassed).isFalse()
    assertThat(outContent.toString()).contains("SVG fallback")
  }

  @Test
  fun testExplorationWithMathInSolution_fails() {
    // Math tag in solution explanation with no LaTeX.
    val explorationJson =
      """
      {
        "exploration_id": "test_exploration",
        "exploration": {
          "init_state_name": "Introduction",
          "states": {
            "Introduction": {
              "content": {
                "content_id": "content",
                "html": "<p>Question</p>"
              },
              "interaction": {
                "answer_groups": [],
                "default_outcome": null,
                "hints": [],
                "solution": {
                  "explanation": {
                    "content_id": "solution",
                    "html": "<oppia-noninteractive-math math_content-with-value=\"{}\"></oppia-noninteractive-math>"
                  }
                }
              }
            }
          }
        }
      }
    """.trimIndent()

    File(assetsDir, "test_exploration.json").writeText(explorationJson)

    val checker = LessonAssetValidationCheck(assetsDir)
    val hasPassed = checker.execute()

    assertThat(hasPassed).isFalse()
    assertThat(outContent.toString()).contains("no LaTeX content")
  }

  @Test
  fun testImageWithTransparentPixels_fails() {
    // Image filepath uses &amp;quot; wrapping, which decodes to quotes that get stripped.
    val explorationJson =
      """
      {
        "exploration_id": "test_exploration",
        "exploration": {
          "init_state_name": "Introduction",
          "states": {
            "Introduction": {
              "content": {
                "content_id": "content",
                "html": "<oppia-noninteractive-image filepath-with-value=\"&amp;quot;transparent_image.png&amp;quot;\"></oppia-noninteractive-image>"
              },
              "interaction": {
                "answer_groups": [],
                "default_outcome": null,
                "hints": [],
                "solution": null
              }
            }
          }
        }
      }
    """.trimIndent()

    File(assetsDir, "test_exploration.json").writeText(explorationJson)

    // Create transparent PNG.
    val image = BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB)
    val g = image.createGraphics()
    g.color = Color(255, 0, 0, 128) // Semi-transparent red (alpha=128).
    g.fillRect(0, 0, 10, 10)
    g.dispose()
    ImageIO.write(image, "PNG", File(assetsDir, "transparent_image.png"))

    val checker = LessonAssetValidationCheck(assetsDir)
    val hasPassed = checker.execute()

    assertThat(hasPassed).isFalse()
    assertThat(outContent.toString()).contains("transparent pixels")
    assertThat(outContent.toString()).contains("dark mode")
  }

  @Test
  fun testImageWithoutTransparentPixels_passes() {
    val explorationJson =
      """
      {
        "exploration_id": "test_exploration",
        "exploration": {
          "init_state_name": "Introduction",
          "states": {
            "Introduction": {
              "content": {
                "content_id": "content",
                "html": "<oppia-noninteractive-image filepath-with-value=\"&amp;quot;opaque_image.png&amp;quot;\"></oppia-noninteractive-image>"
              },
              "interaction": {
                "answer_groups": [],
                "default_outcome": null,
                "hints": [],
                "solution": null
              }
            }
          }
        }
      }
    """.trimIndent()

    File(assetsDir, "test_exploration.json").writeText(explorationJson)

    // Create fully opaque PNG (TYPE_INT_RGB has no alpha channel).
    val image = BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB)
    val g = image.createGraphics()
    g.color = Color.RED
    g.fillRect(0, 0, 10, 10)
    g.dispose()
    ImageIO.write(image, "PNG", File(assetsDir, "opaque_image.png"))

    val checker = LessonAssetValidationCheck(assetsDir)
    val hasPassed = checker.execute()

    assertThat(hasPassed).isTrue()
  }

  @Test
  fun testImageWithAlpha255_passes() {
    val explorationJson =
      """
      {
        "exploration_id": "test_exploration",
        "exploration": {
          "init_state_name": "Introduction",
          "states": {
            "Introduction": {
              "content": {
                "content_id": "content",
                "html": "<oppia-noninteractive-image filepath-with-value=\"&amp;quot;fully_opaque_alpha.png&amp;quot;\"></oppia-noninteractive-image>"
              },
              "interaction": {
                "answer_groups": [],
                "default_outcome": null,
                "hints": [],
                "solution": null
              }
            }
          }
        }
      }
    """.trimIndent()

    File(assetsDir, "test_exploration.json").writeText(explorationJson)

    // Create ARGB image with full alpha (alpha=255).
    val image = BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB)
    val g = image.createGraphics()
    g.color = Color(255, 0, 0, 255) // Fully opaque red.
    g.fillRect(0, 0, 10, 10)
    g.dispose()
    ImageIO.write(image, "PNG", File(assetsDir, "fully_opaque_alpha.png"))

    val checker = LessonAssetValidationCheck(assetsDir)
    val hasPassed = checker.execute()

    assertThat(hasPassed).isTrue()
  }

  @Test
  fun testMultipleMathTagsInState_validatesAll() {
    // Two math tags: first valid, second has no LaTeX.
    val explorationJson =
      """
      {
        "exploration_id": "test_exploration",
        "exploration": {
          "init_state_name": "Introduction",
          "states": {
            "Introduction": {
              "content": {
                "content_id": "content",
                "html": "<oppia-noninteractive-math math_content-with-value=\"{&amp;quot;raw_latex&amp;quot;:&amp;quot;x&amp;quot;}\"></oppia-noninteractive-math><p>and</p><oppia-noninteractive-math math_content-with-value=\"{}\"></oppia-noninteractive-math>"
              },
              "interaction": {
                "answer_groups": [],
                "default_outcome": null,
                "hints": [],
                "solution": null
              }
            }
          }
        }
      }
    """.trimIndent()

    File(assetsDir, "test_exploration.json").writeText(explorationJson)

    val checker = LessonAssetValidationCheck(assetsDir)
    val hasPassed = checker.execute()

    assertThat(hasPassed).isFalse()
    assertThat(outContent.toString()).contains("Math tag #2")
  }

  @Test
  fun testMetadataFilesIgnored() {
    val explorationJson =
      """
      {
        "exploration_id": "test",
        "exploration": {
          "init_state_name": "Introduction",
          "states": {
            "Introduction": {
              "content": {
                "content_id": "content",
                "html": "<p>Test</p>"
              },
              "interaction": {
                "answer_groups": [],
                "default_outcome": null,
                "hints": [],
                "solution": null
              }
            }
          }
        }
      }
    """.trimIndent()

    File(assetsDir, "test.json").writeText(explorationJson)
    // These metadata files should be skipped even if they contain exploration-like JSON.
    File(assetsDir, "classrooms.json").writeText(explorationJson)
    File(assetsDir, "skills.json").writeText(explorationJson)
    File(assetsDir, "questions.json").writeText(explorationJson)

    val checker = LessonAssetValidationCheck(assetsDir)
    val hasPassed = checker.execute()

    assertThat(hasPassed).isTrue()
    // Only 1 file should be validated (not the 3 metadata files).
    assertThat(outContent.toString()).contains("1 exploration file(s)")
  }

  @Test
  fun testInvalidJsonFile_logsError() {
    File(assetsDir, "invalid.json").writeText("{invalid json content")

    val checker = LessonAssetValidationCheck(assetsDir)
    val hasPassed = checker.execute()

    assertThat(hasPassed).isFalse()
    assertThat(outContent.toString()).contains("ERROR")
  }

  @Test
  fun testMissingImageFile_passesGracefully() {
    // When the image file doesn't exist (e.g., dummy assets), validation should not fail.
    val explorationJson =
      """
      {
        "exploration_id": "test_exploration",
        "exploration": {
          "init_state_name": "Introduction",
          "states": {
            "Introduction": {
              "content": {
                "content_id": "content",
                "html": "<oppia-noninteractive-image filepath-with-value=\"&amp;quot;missing_image.png&amp;quot;\"></oppia-noninteractive-image>"
              },
              "interaction": {
                "answer_groups": [],
                "default_outcome": null,
                "hints": [],
                "solution": null
              }
            }
          }
        }
      }
    """.trimIndent()

    File(assetsDir, "test_exploration.json").writeText(explorationJson)
    // Don't create the image file — simulates dummy assets environment.

    val checker = LessonAssetValidationCheck(assetsDir)
    val hasPassed = checker.execute()

    assertThat(hasPassed).isTrue()
  }

  @Test
  fun testSelfClosingMathTag_validatesCorrectly() {
    // Some math tags use self-closing syntax (/>), matching real data in GJ2rLXRKD5hw_1.json.
    val explorationJson =
      """
      {
        "exploration_id": "test_exploration",
        "exploration": {
          "init_state_name": "Introduction",
          "states": {
            "Introduction": {
              "content": {
                "content_id": "content",
                "html": "<oppia-noninteractive-math render-type=\"inline\" math_content-with-value=\"{&amp;quot;raw_latex&amp;quot;:&amp;quot;y=mx+b&amp;quot;}\" />"
              },
              "interaction": {
                "answer_groups": [],
                "default_outcome": null,
                "hints": [],
                "solution": null
              }
            }
          }
        }
      }
    """.trimIndent()

    File(assetsDir, "test_exploration.json").writeText(explorationJson)

    val checker = LessonAssetValidationCheck(assetsDir)
    val hasPassed = checker.execute()

    assertThat(hasPassed).isTrue()
  }

  @Test
  fun testHtmlEntityDecodingInMathContent_passes() {
    // Verify &amp;quot; entities are properly decoded for math content parsing.
    val explorationJson =
      """
      {
        "exploration_id": "test_exploration",
        "exploration": {
          "init_state_name": "Introduction",
          "states": {
            "Introduction": {
              "content": {
                "content_id": "content",
                "html": "<oppia-noninteractive-math math_content-with-value=\"{&amp;quot;raw_latex&amp;quot;:&amp;quot;\\\\sqrt{x}&amp;quot;}\"></oppia-noninteractive-math>"
              },
              "interaction": {
                "answer_groups": [],
                "default_outcome": null,
                "hints": [],
                "solution": null
              }
            }
          }
        }
      }
    """.trimIndent()

    File(assetsDir, "test_exploration.json").writeText(explorationJson)

    val checker = LessonAssetValidationCheck(assetsDir)
    val hasPassed = checker.execute()

    assertThat(hasPassed).isTrue()
  }
}
