param(
 [Parameter(Mandatory=$true)][string]$GameDirectory,
 [Parameter(Mandatory=$true)][string]$JavaPath,
 [string]$PackUrl='https://shinozakiaichi.github.io/zombkotom-updates/pack.toml'
)
$ErrorActionPreference='Stop'
[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12
$updateLock=$null
function FileHash([string]$Path,[string]$Algorithm='SHA256') {
 $stream=[IO.File]::OpenRead($Path);$digest=[Security.Cryptography.HashAlgorithm]::Create($Algorithm)
 try {return ([BitConverter]::ToString($digest.ComputeHash($stream))).Replace('-','').ToLowerInvariant()}
 finally {$stream.Dispose();$digest.Dispose()}
}
try {
 $gameRoot=(Resolve-Path -LiteralPath $GameDirectory).Path
 $updateLock=[IO.File]::Open((Join-Path $gameRoot '.lastbastion-update.lock'),'OpenOrCreate','ReadWrite','None')
 $javaExe=$JavaPath
 if([IO.Path]::GetFileName($javaExe) -eq 'javaw.exe'){$javaExe=Join-Path ([IO.Path]::GetDirectoryName($javaExe)) 'java.exe'}
 if(-not(Test-Path -LiteralPath $javaExe)){throw 'Configured Java was not found.'}
 $hashes=Get-Content -LiteralPath (Join-Path $gameRoot 'lastbastion-tools.json') -Raw | ConvertFrom-Json
 foreach($item in $hashes.PSObject.Properties){
  if((FileHash (Join-Path $gameRoot $item.Name)) -ne $item.Value){throw "Updater integrity check failed: $($item.Name)"}
 }
 # A running world must be saved and closed before changing its modpack.
 $saves=Join-Path $gameRoot 'saves'
 if(Test-Path -LiteralPath $saves){
  foreach($world in Get-ChildItem -LiteralPath $saves -Directory){
   $sessionFile=Join-Path $world.FullName 'session.lock'
   if(Test-Path -LiteralPath $sessionFile){$worldLock=[IO.File]::Open($sessionFile,'Open','ReadWrite','None');$worldLock.Dispose()}
  }
 }
 Write-Output '[LAST BASTION] Checking the published release...'
 $packText=(Invoke-WebRequest -Uri $PackUrl -UseBasicParsing -TimeoutSec 45).Content
 $sha=[Security.Cryptography.SHA256]::Create()
 $packHash=([BitConverter]::ToString($sha.ComputeHash([Text.Encoding]::UTF8.GetBytes([string]$packText)))).Replace('-','').ToLowerInvariant();$sha.Dispose()
 $stamp=Join-Path $gameRoot '.lastbastion-published-hash'
 if(-not(Test-Path -LiteralPath $stamp) -or (Get-Content -LiteralPath $stamp -Raw).Trim() -ne $packHash){
  $backup=Join-Path $gameRoot ('update-backups\auto-'+$packHash.Substring(0,16))
  if(-not(Test-Path -LiteralPath $backup)){
   New-Item -ItemType Directory -Path $backup -Force | Out-Null
   foreach($n in @('config','packwiz.json')){
    $old=Join-Path $gameRoot $n
    if(Test-Path -LiteralPath $old){Copy-Item -LiteralPath $old -Destination (Join-Path $backup $n) -Recurse}
   }
  }
 }
 $bootstrap=Join-Path $gameRoot 'packwiz-installer-bootstrap.jar'
 $installer=Join-Path $gameRoot 'packwiz-installer.jar'
 $log=Join-Path $gameRoot 'lastbastion-update.log'
 Push-Location -LiteralPath $gameRoot
 try {
  $ErrorActionPreference='Continue'
  & $javaExe '-Xmx512M' '-jar' $bootstrap '--bootstrap-no-update' '--bootstrap-main-jar' $installer '-g' '--pack-folder' $gameRoot $PackUrl 2>&1 | Tee-Object -FilePath $log
  $code=$LASTEXITCODE
  $ErrorActionPreference='Stop'
 } finally {Pop-Location}
 if($code -ne 0){throw "Update failed (exit $code). Minecraft launch is cancelled; retry when the download works."}
 # One-time adoption of older ZIP installs. Never remove unlisted personal files.
 $retiredFile=Join-Path $gameRoot 'lastbastion-retired-mods.json'
 if(Test-Path -LiteralPath $retiredFile){
  foreach($mod in (Get-Content -LiteralPath $retiredFile -Raw | ConvertFrom-Json)){
   if([IO.Path]::GetFileName($mod.filename) -ne $mod.filename){throw 'Invalid retired mod path'}
   $old=Join-Path (Join-Path $gameRoot 'mods') $mod.filename
   if(Test-Path -LiteralPath $old){
    if((FileHash $old 'SHA512') -ne $mod.sha512){throw "Unrecognized old mod: $($mod.filename). Move it outside mods and retry."}
    $archive=Join-Path $gameRoot ('update-backups\retired-'+[DateTime]::Now.ToString('yyyyMMdd-HHmmss-ffff'))
    New-Item -ItemType Directory -Path $archive -Force | Out-Null
    Move-Item -LiteralPath $old -Destination (Join-Path $archive $mod.filename)
   }
  }
 }
 # One-time default shader download from the author's Modrinth CDN. Never overwrite a shader.
 $defaultsMarker=Join-Path $gameRoot '.lastbastion-default-shader-ready'
 $initial=Join-Path $gameRoot 'lastbastion-initial-downloads.json'
 if((Test-Path -LiteralPath $initial) -and -not(Test-Path -LiteralPath $defaultsMarker)){
  $shaderRoot=Join-Path $gameRoot 'shaderpacks'
  New-Item -ItemType Directory -Path $shaderRoot -Force | Out-Null
  foreach($item in (Get-Content -LiteralPath $initial -Raw | ConvertFrom-Json)){
   if([IO.Path]::GetFileName($item.filename) -ne $item.filename){throw 'Invalid initial shader path'}
   $target=Join-Path $shaderRoot $item.filename
   if(-not(Test-Path -LiteralPath $target)){
    $temp=$target+'.lastbastion-download'
    Invoke-WebRequest -Uri $item.url -UseBasicParsing -TimeoutSec 90 -OutFile $temp
    if((FileHash $temp 'SHA512') -ne $item.sha512){throw 'Initial shader download hash mismatch'}
    Move-Item -LiteralPath $temp -Destination $target
   }
  }
  [IO.File]::WriteAllText($defaultsMarker,'Initialized; shaderpacks is not managed by future updates.')
 }
 [IO.File]::WriteAllText($stamp,$packHash)
 Write-Output '[LAST BASTION] Update complete. Personal shaders and settings are outside the update index.'
 exit 0
} catch {
 Write-Output ('[LAST BASTION] STOP: '+$_.Exception.Message)
 exit 1
} finally {if($null -ne $updateLock){$updateLock.Dispose()}}
