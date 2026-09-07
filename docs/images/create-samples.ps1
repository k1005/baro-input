# Exact text fixtures for the user guide. Run on Windows with Malgun Gothic.
Add-Type -AssemblyName System.Drawing
$samples = @(
    @{ Name = 'sample-numbers.png'; Lines = @('0012 3456 7890') },
    @{ Name = 'sample-product-code.png'; Lines = @('BARO-2026-0012') },
    @{ Name = 'sample-text.png'; Lines = @('바로입력 테스트'; '오늘도 좋은 하루'; 'Hello BARO 2026') }
)
foreach ($sample in $samples) {
    $height = 180 + 120 * $sample.Lines.Count
    $bitmap = [System.Drawing.Bitmap]::new(1200, $height)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $font = [System.Drawing.Font]::new('Malgun Gothic', 76, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
    $format = [System.Drawing.StringFormat]::new()
    try {
        $graphics.Clear([System.Drawing.Color]::White)
        $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
        $format.Alignment = [System.Drawing.StringAlignment]::Center
        $format.LineAlignment = [System.Drawing.StringAlignment]::Center
        for ($index = 0; $index -lt $sample.Lines.Count; $index++) {
            $rect = [System.Drawing.RectangleF]::new(50, (90 + $index * 120), 1100, 120)
            $graphics.DrawString($sample.Lines[$index], $font, [System.Drawing.Brushes]::Black, $rect, $format)
        }
        $bitmap.Save((Join-Path $PSScriptRoot $sample.Name), [System.Drawing.Imaging.ImageFormat]::Png)
    } finally {
        $format.Dispose()
        $font.Dispose()
        $graphics.Dispose()
        $bitmap.Dispose()
    }
}
