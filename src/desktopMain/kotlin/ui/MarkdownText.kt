package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

/**
 * Composable функция для отображения Markdown текста.
 *
 * @param markdown Строка в формате Markdown
 * @param modifier Модификатор для стилизации
 * @param color Цвет текста по умолчанию
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    val flavour = CommonMarkFlavourDescriptor()
    val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(markdown)

    Column(modifier = modifier) {
        parsedTree.children.forEach { node ->
            RenderNode(node, markdown, color)
        }
    }
}

@Composable
private fun RenderNode(
    node: ASTNode,
    markdown: String,
    defaultColor: Color = Color.Unspecified,
    inlineModifier: Modifier = Modifier
) {
    when (node.type) {
        MarkdownElementTypes.PARAGRAPH -> {
            RenderParagraph(node, markdown, defaultColor)
        }
        MarkdownElementTypes.ATX_1 -> {
            RenderHeading(node, markdown, 1, defaultColor)
        }
        MarkdownElementTypes.ATX_2 -> {
            RenderHeading(node, markdown, 2, defaultColor)
        }
        MarkdownElementTypes.ATX_3 -> {
            RenderHeading(node, markdown, 3, defaultColor)
        }
        MarkdownElementTypes.ATX_4 -> {
            RenderHeading(node, markdown, 4, defaultColor)
        }
        MarkdownElementTypes.ATX_5 -> {
            RenderHeading(node, markdown, 5, defaultColor)
        }
        MarkdownElementTypes.ATX_6 -> {
            RenderHeading(node, markdown, 6, defaultColor)
        }
        MarkdownElementTypes.CODE_FENCE -> {
            RenderCodeBlock(node, markdown)
        }
        MarkdownElementTypes.CODE_BLOCK -> {
            RenderCodeBlock(node, markdown)
        }
        MarkdownElementTypes.UNORDERED_LIST -> {
            RenderList(node, markdown, ordered = false, defaultColor = defaultColor)
        }
        MarkdownElementTypes.ORDERED_LIST -> {
            RenderList(node, markdown, ordered = true, defaultColor = defaultColor)
        }
        MarkdownElementTypes.BLOCK_QUOTE -> {
            RenderBlockQuote(node, markdown, defaultColor)
        }
    }
}

@Composable
private fun RenderParagraph(
    node: ASTNode,
    markdown: String,
    defaultColor: Color
) {
    val codeBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val codeTextColor = MaterialTheme.colorScheme.primary
    val linkColor = MaterialTheme.colorScheme.primary

    val annotatedString = buildAnnotatedString {
        RenderInlineContent(
            node,
            markdown,
            this,
            defaultColor,
            codeBackgroundColor,
            codeTextColor,
            linkColor
        )
    }

    if (annotatedString.text.isNotBlank()) {
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium,
            color = defaultColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun RenderHeading(
    node: ASTNode,
    markdown: String,
    level: Int,
    defaultColor: Color
) {
    val codeBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val codeTextColor = MaterialTheme.colorScheme.primary
    val linkColor = MaterialTheme.colorScheme.primary

    val annotatedString = buildAnnotatedString {
        RenderInlineContent(node, markdown, this, defaultColor, codeBackgroundColor, codeTextColor, linkColor)
    }

    val style = when (level) {
        1 -> MaterialTheme.typography.headlineLarge
        2 -> MaterialTheme.typography.headlineMedium
        3 -> MaterialTheme.typography.headlineSmall
        4 -> MaterialTheme.typography.titleLarge
        5 -> MaterialTheme.typography.titleMedium
        else -> MaterialTheme.typography.titleSmall
    }

    Text(
        text = annotatedString,
        style = style,
        color = defaultColor,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun RenderCodeBlock(
    node: ASTNode,
    markdown: String
) {
    val code = node.getTextInNode(markdown).toString()
        .removePrefix("```")
        .removeSuffix("```")
        .trim()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .horizontalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RenderList(
    node: ASTNode,
    markdown: String,
    ordered: Boolean,
    defaultColor: Color
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        var index = 1
        node.children.forEach { listItem ->
            if (listItem.type == MarkdownElementTypes.LIST_ITEM) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                ) {
                    val bullet = if (ordered) "${index++}. " else "• "
                    Text(
                        text = bullet,
                        color = defaultColor,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    val itemContent = buildAnnotatedString {
                        RenderInlineContent(listItem, markdown, this, defaultColor)
                    }

                    Text(
                        text = itemContent,
                        color = defaultColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderBlockQuote(
    node: ASTNode,
    markdown: String,
    defaultColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 8.dp)
    ) {
        val quoteColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        val content = buildAnnotatedString {
            RenderInlineContent(node, markdown, this, quoteColor)
        }

        Text(
            text = content,
            color = quoteColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun RenderInlineContent(
    node: ASTNode,
    markdown: String,
    builder: AnnotatedString.Builder,
    defaultColor: Color,
    codeBackgroundColor: Color = Color(0xFFF5F5F5),
    codeTextColor: Color = Color(0xFFD73A49),
    linkColor: Color = Color(0xFF007AFF)
) {
    node.children.forEach { child ->
        when (child.type) {
            MarkdownElementTypes.STRONG -> {
                builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    RenderInlineContent(child, markdown, builder, defaultColor, codeBackgroundColor, codeTextColor, linkColor)
                }
            }
            MarkdownElementTypes.EMPH -> {
                builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    RenderInlineContent(child, markdown, builder, defaultColor, codeBackgroundColor, codeTextColor, linkColor)
                }
            }
            MarkdownElementTypes.CODE_SPAN -> {
                builder.withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = codeBackgroundColor,
                        color = codeTextColor
                    )
                ) {
                    val code = child.getTextInNode(markdown).toString()
                        .removePrefix("`")
                        .removeSuffix("`")
                    append(code)
                }
            }
            MarkdownElementTypes.INLINE_LINK -> {
                val linkText = child.children.firstOrNull { it.type == MarkdownElementTypes.LINK_TEXT }

                if (linkText != null) {
                    builder.withStyle(
                        SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        RenderInlineContent(linkText, markdown, builder, defaultColor, codeBackgroundColor, codeTextColor, linkColor)
                    }
                }
            }
            MarkdownTokenTypes.TEXT,
            MarkdownElementTypes.LINK_TEXT,
            MarkdownTokenTypes.SINGLE_QUOTE,
            MarkdownTokenTypes.DOUBLE_QUOTE,
            MarkdownTokenTypes.LPAREN,
            MarkdownTokenTypes.RPAREN,
            MarkdownTokenTypes.LBRACKET,
            MarkdownTokenTypes.RBRACKET,
            MarkdownTokenTypes.EXCLAMATION_MARK,
            MarkdownTokenTypes.COLON,
            MarkdownTokenTypes.WHITE_SPACE -> {
                builder.append(child.getTextInNode(markdown).toString())
            }
            else -> {
                RenderInlineContent(child, markdown, builder, defaultColor, codeBackgroundColor, codeTextColor, linkColor)
            }
        }
    }
}