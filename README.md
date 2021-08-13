# Note-ingHill

## Setup instructions (Linux)
### 1. Pre-requisites (**IMPORTANT**)
- Node.js v12.x or later (Link : https://nodejs.org/en/download/)
- npm v5.x or later (npm installs as a package along with Node)
- git v2.14.1 or later

### 2. Open Terminal
- Run `npm install -g @aws-amplify/cli`
(Global installation of AWS Amplify CLI)
- Run `amplify configure`
- Sign in with your AWS credentials on the browser and then press enter in terminal
- Select ap-south-1
- Set a new user name (At this point, it is recommended to call Aniketh. A new IAM user will be created. That will need granting permissions. Aniketh will take the reins from here!)

### 3. Open Android Studio
- Select "Get from Version Control"
- Link GitHub account using login or token (Access token is most likely to work)
- Paste GitHub repo URL: `https://github.com/Aniket-512/Note-ingHill/`
- Click "Clone"

### Tips
- Run Android Studio as a Root user (Linux) or as an Administrator (Windows).
- (For Windows only) Check your path variables. If node path is not present, add it to your path.
- (For Windows Only) If you get a EEXIST: File already exists error, make sure you contact Vishnu. In your C:/Users/<UserName> path, your username has a space. Refer to this link : https://docs.microsoft.com/en-US/troubleshoot/windows-client/user-profiles-and-logon/renaming-user-account-not-change-profile-path
- Do not update anything unless it is absolutely necessary. (Updating IDE is fine, but ask before you update gradle versions. Incorrect gradel versions will not be compatible in VC)
