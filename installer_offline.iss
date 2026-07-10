; Race Coordinator AI Offline Installer Script
; Bundles modern and legacy dependencies (JRE 8/17, MongoDB 3.2/6.0)
; Only installs the version appropriate for the current OS

#include "installer_base.iss"

[Setup]
OutputBaseFilename=RaceCoordinatorAI_Offline_Setup

[Files]
; Modern OS (Win10+)
Source: "release\RaceCoordinator\jre17\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs skipifsourcedoesntexist; Check: IsWindows10OrNewer
Source: "release\RaceCoordinator\mongodb60\*"; DestDir: "{app}\mongodb"; Flags: ignoreversion recursesubdirs createallsubdirs skipifsourcedoesntexist; Check: IsWindows10OrNewer

; Legacy OS
Source: "release\RaceCoordinator\jre8\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs skipifsourcedoesntexist; Check: not IsWindows10OrNewer
Source: "release\RaceCoordinator\mongodb32\*"; DestDir: "{app}\mongodb"; Flags: ignoreversion recursesubdirs createallsubdirs skipifsourcedoesntexist; Check: not IsWindows10OrNewer
