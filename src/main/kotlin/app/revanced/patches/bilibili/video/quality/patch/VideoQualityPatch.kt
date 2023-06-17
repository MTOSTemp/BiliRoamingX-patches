package app.revanced.patches.bilibili.video.quality.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.bilibili.video.quality.annotations.VideoQualityCompatibility
import app.revanced.patches.bilibili.video.quality.fingerprints.PlayerPreloadHolderFingerprint
import app.revanced.patches.bilibili.video.quality.fingerprints.PlayerQualityServiceFingerprint
import app.revanced.patches.bilibili.video.quality.fingerprints.PlayerSettingHelperFingerprint
import org.jf.dexlib2.iface.Method

@Patch
@Name("video-default-quality")
@VideoQualityCompatibility
@Description("视频默认画质设置")
@Version("0.0.1")
class VideoQualityPatch : BytecodePatch(
    listOf(
        PlayerSettingHelperFingerprint,
        PlayerPreloadHolderFingerprint,
        PlayerQualityServiceFingerprint,
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        var defaultQnMethod: Method? = null
        PlayerSettingHelperFingerprint.result?.also {
            defaultQnMethod = it.method
        }?.mutableMethod?.addInstructions(
            0, """
                    invoke-static {}, Lapp/revanced/bilibili/patches/VideoQualityPatch;->fullScreenQuality()I
                    move-result v0
                    if-eqz v0, :jump
                    return v0
                    :jump
                    nop
                """.trimIndent()
        ) ?: return PlayerSettingHelperFingerprint.toErrorResult()
        PlayerPreloadHolderFingerprint.result?.mutableMethod?.addInstructions(
            0, """
                    invoke-static {}, Lapp/revanced/bilibili/patches/VideoQualityPatch;->halfScreenQuality()I
                    move-result v0
                    if-eqz v0, :jump
                    const/4 v0, 0x0
                    return-object v0
                    :jump
                    nop
            """.trimIndent()
        ) ?: return PlayerPreloadHolderFingerprint.toErrorResult()
        PlayerQualityServiceFingerprint.result?.mutableMethod?.addInstructions(
            0, """
                    invoke-static {}, Lapp/revanced/bilibili/patches/VideoQualityPatch;->getMatchedHalfScreenQuality()I
                    move-result v0
                    if-eqz v0, :jump
                    return v0
                    :jump
                    nop
        """.trimIndent()
        ) ?: return PlayerQualityServiceFingerprint.toErrorResult()
        context.findClass("Lapp/revanced/bilibili/patches/VideoQualityPatch;")?.let { clazz ->
            clazz.mutableClass.methods.find { it.name == "defaultQn" }?.addInstructions(
                0, """
                invoke-static {}, $defaultQnMethod
                move-result v0
                return v0
                """.trimIndent()
            )
        }
        return PatchResultSuccess()
    }
}