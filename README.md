# UNO

Ein ITP-Schulprojekt der **HTBLA Steyr**

---

## Über das Projekt

UNO ist eine digitale Umsetzung des gleichnamigen Kartenspiels, entwickelt von 4 Schülern der HTBLA Steyr im Rahmen des ITP-Unterrichts (Informationstechnische Projekte).

Das Spiel ist **rein auf Multiplayer ausgelegt**: Ein **Singleplayer-Modus ist nicht verfügbar**. Eine Runde kann nur gestartet werden, wenn **mindestens 2 und maximal 4 Spieler** in einer Lobby sind.

Die Kommunikation zwischen Client und Server erfolgt über eine Socket-Verbindung. Als Server dient ein Docker-Container, der auf einem Raspberry Pi betrieben wird — dadurch ist das Spiel jederzeit erreichbar, ohne dass ein eigener Rechner laufen muss.

Das Projekt wurde innerhalb einer Zeitvorgabe von 2 Monaten gestartet und wird seither laufend weiterentwickelt. Der Fokus liegt auf einer stabilen Serverarchitektur, einer benutzerfreundlichen JavaFX-Oberfläche sowie einer sauberen Umsetzung der UNO-Spielregeln.

---

## Features

- **Multiplayer (2–4 Spieler)** über Lobbys (Lobby erstellen / Lobby beitreten)
- **Lobby-Chat** zum Schreiben mit allen Spielern in der Lobby
- **Account-System** (Registrieren / Login)
- **Passwort vergessen** inkl. Code-Verifikation und Passwort ändern
- **Profilbild ändern**: Nach dem Anmelden im Lobby-Screen auf das **Profilbild klicken** und ein Bild auswählen
- **Spiel-Statistiken** im Lobby-Screen:
  - **Spiele gesamt**
  - **Siege**
- UNO-GameTable mit Gegner-Anzeige (Benutzername + Profilbild) und Spielstatus-Updates in Echtzeit

---

## Spielregeln

Das Spiel folgt den offiziellen UNO-Regeln: Jeder Spieler startet mit 7 Karten und muss reihum eine Karte legen, die entweder **Farbe oder Zahl** der obersten Ablagestapelkarte entspricht. Aktionskarten wie **Zieh Zwei**, **Aussetzen** oder **Richtungswechsel** sorgen für Spannung — und mit **+4** oder **Farbwechsel** lässt sich das Blatt schnell wenden. Wer nur noch eine Karte hat, muss laut **„UNO!"** rufen. Wer zuerst alle Karten ablegt, gewinnt die Runde.

Das vollständige Regelwerk findest du hier: [Regelwerk](./REGELWERK.md)

---

## Technologien

![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-007396?style=for-the-badge&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![CSS](https://img.shields.io/badge/CSS-1572B6?style=for-the-badge&logo=css3&logoColor=white)

---

## Installation

1. Gehe zu [Releases](../../releases/latest)
2. Lade die neueste `.exe` Datei herunter
3. Führe die `.exe` aus und folge dem Installationsassistenten
4. Starte das Programm

---

## Ordnerstruktur

<details>
<summary>Ordnerstruktur anzeigen</summary>

```
uno/
├── .github/
│   ├── ISSUE_TEMPLATE/
│   │   ├── bug_report.md
│   │   ├── custom.md
│   │   └── feature_request.md
│   ├── workflows/
│   │   ├── client-release.yml
│   │   ├── push-ghcr.yml
│   │   └── trigger.txt
│   └── PULL_REQUEST_TEMPLATE.md
│
├── src/main/java/htl.steyr.uno/
│   ├── client/
│   │   ├── Client.java
│   │   └── ClientSocketConnection.java
│   │
│   ├── GameTableClasses/
│   │   ├── Card.java
│   │   ├── CardStack.java
│   │   ├── Enemy.java
│   │   ├── EnemyDisplayController.java
│   │   ├── GameLogic.java
│   │   ├── GameTable.java
│   │   └── Player.java
│   │
│   ├── Lobby/
│   │   ├── LobbyTestApplication.java
│   │   ├── LobbyTestLauncher.java
│   │   └── LobbyWaitController.java
│   │
│   ├── requests/
│   │   ├── client/
│   │   │   ├── CardPlayedRequest.java
│   │   │   ├── ChangePasswordRequest.java
│   │   │   ├── CheckIfUserAlreadyExistsRequest.java
│   │   │   ├── CreateAccountRequest.java
│   │   │   ├── CreateLobbyRequest.java
│   │   │   ├── ForgotPasswordRequest.java
│   │   │   ├── ForgotPasswordSendCodeRequest.java
│   │   │   ├── JoinLobbyRequest.java
│   │   │   ├── LeaveLobbyRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── ReadyInGameTableRequest.java
│   │   │   ├── RequestCardRequest.java
│   │   │   ├── SendChatMessageRequest.java
│   │   │   ├── SetProfileImageRequest.java
│   │   │   └── StartGameRequest.java
│   │   │
│   │   └── server/
│   │       ├── CardAddResponse.java
│   │       ├── CheckIfUserAlreadyExistsResponse.java
│   │       ├── CreateAccountFailedResponse.java
│   │       ├── CreateAccountSuccessResponse.java
│   │       ├── EnemyDrawnCardsResponse.java
│   │       ├── ForgotPasswordResponse.java
│   │       ├── GameTurnResponse.java
│   │       ├── LobbyInfoResponse.java
│   │       ├── LobbyJoinRefusedResponse.java
│   │       ├── LobbyNotFoundResponse.java
│   │       ├── LoginFailedResponse.java
│   │       ├── LoginSuccessResponse.java
│   │       ├── PlayerGetResponse.java
│   │       ├── ReceiveChatMessageResponse.java
│   │       ├── StackInfoResponse.java
│   │       ├── StartGameResponse.java
│   │       └── UpdateEnemyResponse.java
│   │
│   └── server/
│       ├── database/
│       │   ├── DatabaseConnection.java
│       │   ├── DatabaseLog.java
│       │   └── DatabaseUser.java
│       ├── exceptions.database/
│       │   ├── DatabaseException.java
│       │   ├── UserAlreadyExistsException.java
│       │   └── UserException.java
│       ├── serverconnection/
│       │   ├── CardDeck.java
│       │   ├── GameLogic.java
│       │   ├── Lobby.java
│       │   ├── MailSender.java
│       │   ├── PasswordForgotten.java
│       │   ├── Server.java
│       │   └── ServerSocketConnection.java
│       ├── HelloApplication.java
│       ├── Launcher.java
│       ├── LobbyController.java
│       ├── LoginController.java
│       ├── PasswordUtil.java
│       ├── UiStyleUtil.java
│       └── User.java
│
├── src/main/resources/htl.steyr.uno/
│   ├── img/
│   │   └── profile.png
│   ├── style/
│   │   ├── enemy.css
│   │   ├── GameTableStyle.css
│   │   ├── globalFocus.css
│   │   ├── lobbyStyle.css
│   │   ├── lobbyWaitStyle.css
│   │   └── loginStyle.css
│   ├── Uno_Cards/
│   │   ├── black/
│   │   ├── blue/
│   │   ├── green/
│   │   ├── red/
│   │   ├── yellow/
│   │   └── backside.png
│   ├── enemy.fxml
│   ├── gameTable.fxml
│   ├── lobby.fxml
│   ├── lobbyWait.fxml
│   └── loginScreen.fxml
│
├── .gitignore
├── CODE_OF_CONDUCT.md
├── CONTRIBUTING.md
├── Dockerfile
├── LICENSE
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
├── REGELWERK.md
└── SECURITY.md
```

</details>

---

## Autoren

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/MarcelStuebl">
        <img src="https://github.com/MarcelStuebl.png" width="100px;" style="border-radius:50%;" alt="Marcel Stübl"/>
      </a>
      <br />
      <h3>Marcel STÜBL</h3>
      <img src="https://img.shields.io/badge/Server-2D2D2D?style=for-the-badge&logo=serverfault&logoColor=white" alt="Server"/>
      <img src="https://img.shields.io/badge/Backend-007396?style=for-the-badge&logo=java&logoColor=white" alt="Backend"/>
      <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
      <img src="https://img.shields.io/badge/Docs-7048E8?style=for-the-badge&logo=readthedocs&logoColor=white" alt="Docs"/>
      <br /><br />
      <a href="mailto:mstuebl@htl-steyr.ac.at">mstuebl@htl-steyr.ac.at</a>
    </td>
    <td align="center">
      <a href="https://github.com/1Mauritz2">
        <img src="https://github.com/1Mauritz2.png" width="100px;" style="border-radius:50%;" alt="Moritz Raschko"/>
      </a>
      <br />
      <h3>Moritz RASCHKO</h3>
      <img src="https://img.shields.io/badge/Game%20Rules-087F5B?style=for-the-badge&logo=gnometerminal&logoColor=white" alt="Game Rules"/>
      <img src="https://img.shields.io/badge/Frontend-FF6B6B?style=for-the-badge&logo=css3&logoColor=white" alt="Frontend"/>
      <img src="https://img.shields.io/badge/Backend-007396?style=for-the-badge&logo=java&logoColor=white" alt="Backend"/>
      <img src="https://img.shields.io/badge/Design-5C7CFA?style=for-the-badge&logo=figma&logoColor=white" alt="Design"/>
      <img src="https://img.shields.io/badge/Docs-7048E8?style=for-the-badge&logo=readthedocs&logoColor=white" alt="Docs"/>
      <br /><br />
      <a href="mailto:mraschk1@htl-steyr.ac.at">mraschk1@htl-steyr.ac.at</a>
    </td>
    <td align="center">
      <a href="https://github.com/DerSchnetzelhold">
        <img src="https://github.com/DerSchnetzelhold.png" width="100px;" style="border-radius:50%;" alt="Jonas Fürlinger"/>
      </a>
      <br />
      <h3>Jonas FÜRLINGER</h3>
      <img src="https://img.shields.io/badge/Game%20Rules-087F5B?style=for-the-badge&logo=gnometerminal&logoColor=white" alt="Game Rules"/>
      <img src="https://img.shields.io/badge/Backend-007396?style=for-the-badge&logo=java&logoColor=white" alt="Backend"/>
      <img src="https://img.shields.io/badge/Frontend-FF6B6B?style=for-the-badge&logo=css3&logoColor=white" alt="Frontend"/>
      <img src="https://img.shields.io/badge/Docs-7048E8?style=for-the-badge&logo=readthedocs&logoColor=white" alt="Docs"/>
      <br /><br />
      <a href="mailto:jfuerlin@htl-steyr.ac.at">jfuerlin@htl-steyr.ac.at</a>
    </td>
    <td align="center">
      <a href="https://github.com/BR00TManu">
        <img src="https://github.com/BR00TManu.png" width="100px;" style="border-radius:50%;" alt="Manuel Horeth"/>
      </a>
      <br />
      <h3>Manuel HORETH</h3>
      <img src="https://img.shields.io/badge/Logic-0CA678?style=for-the-badge&logo=codeforces&logoColor=white" alt="Logic"/>
      <img src="https://img.shields.io/badge/Backend-007396?style=for-the-badge&logo=java&logoColor=white" alt="Backend"/>
      <img src="https://img.shields.io/badge/Frontend-FF6B6B?style=for-the-badge&logo=css3&logoColor=white" alt="Frontend"/>
      <img src="https://img.shields.io/badge/Docs-7048E8?style=for-the-badge&logo=readthedocs&logoColor=white" alt="Docs"/>
      <br /><br />
      <a href="mailto:mhoreth@htl-steyr.ac.at">mhoreth@htl-steyr.ac.at</a>
    </td>
  </tr>
</table>

## Lizenz

Dieses Projekt steht unter der [MIT License](LICENSE).

---

