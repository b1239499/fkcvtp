# fkcvtp（Folia 1.21.8 相容）

隱身狀態與傳送、保護區繞過權限綁定在一起的巡查插件。

## 邏輯
- `/fkcvanish`：切換隱身。開啟時會隱藏你、附加暫時權限（傳送 + config.yml 設定的保護區 bypass 權限）。
  再次輸入 `/fkcvanish` 預設**不會**關閉隱身（除非你有 `vanishtp.forceoff` 權限），因為規格要求隱身只能透過
  `/fkctpa` 被接受，或 `/fkcspawn` 才能解除。
- `/fkcvtp <玩家>`：只有在隱身狀態下才能使用，直接非同步傳送到對方身邊。
- `/fkcvtp <x> <y> <z> [世界名稱]`：只有在隱身狀態下才能用，**還需要額外的 `vanishtp.tpcoords` 權限**，
  直接傳送到指定座標。不寫世界名稱的話會用你目前所在的世界；要跨世界傳就在最後多打一個世界名稱
  （跟 `server.properties` / 資料夾名稱一致，例如 `world`、`world_nether`、`world_the_end`）。
- 隱身期間會自動取得 config.yml 的 `bypass-permissions` 清單裡的權限節點，讓你能無視保護區的移動/傳送限制
  （實際能不能繞過，取決於你的保護區插件是不是用「權限節點」判斷 bypass，詳見下方）。
- `/fkctpa <玩家>`：發出傳送請求，對方用 `/fkctpaccept` 接受 或 `/fkctpdeny` 拒絕。
  **一旦被接受**，你會被傳送過去，且如果你原本是隱身狀態，會自動解除隱身。
- `/fkcspawn`：傳送回設定好的重生點，同樣會自動解除隱身。
- `/fkcgod`：切換無敵狀態。開啟後完全免疫任何傷害來源；**只要你對其他生物造成傷害，就會自動解除無敵**
  （攻擊生物、玩家、用箭射人都算，只要真的打中造成傷害）。
- **隱身狀態下無法撿取地上的物品**（直接攔截取消，不會額外跳出任何提示訊息）。
- `/fkclocate <地形名稱>`：搜尋離你最近的指定地形，回報座標。需要 `vanishtp.locate` 權限，跟隱身狀態無關，
  隨時都能用。地形名稱要用原版小寫英文 ID（例如 `desert`、`jungle`、`plains`、`ocean`、`savanna`）。
  這個指令內部是借用原版 `/locate biome` 的搜尋邏輯（Mojang 官方實作，效能與 Folia 相容性都有保證），
  只是繞過玩家本身需要的原版權限，改用我們自己的 `vanishtp.locate` 控管。
- **手持屏障方塊時可以直接破壞屏障方塊**：需要 `vanishtp.breakbarrier` 權限，且主手拿著屏障方塊才會生效，
  跟隱身狀態無關。走的是正常的方塊破壞流程（`BlockDamageEvent` 設為瞬間破壞），破壞後仍會觸發
  `BlockBreakEvent`，所以你的保護區插件（WorldGuard 等）一樣能照常攔截這個動作；預設不會有掉落物。

### 通用版「解除隱身」（重要）
如果你伺服器上已經有另一套 `/tpa` `/spawn` 系統（例如 Essentials 之類的插件），玩家應該直接用**那一套**
既有的傳送介面，不需要用 `/fkctpa` `/fkcspawn`。為了配合這種情況，插件額外監聽了
`PlayerTeleportEvent`：**只要隱身中的玩家透過任何「指令」被成功傳送（`TeleportCause.COMMAND`），
不管是哪個插件的哪個指令觸發的，都會自動解除隱身**。這樣就不用去搶別的插件已經佔用的指令名稱
（例如 `/tpaccept` `/tpyes` `/tpdeny` `/tpno`），也能達到「透過 tpa 被接受或 spawn 才能解除隱身」的效果。

我們自己的 `/fkcvtp`（隱身中直接傳送到玩家身邊）用的是預設的 `TeleportCause.PLUGIN`，不會被這個監聽器
誤判成「該解除隱身了」。`/fkctpa` `/fkcspawn` 這幾個指令保留下來，給沒有另外裝 tpa/spawn 插件的伺服器
當作備用方案，兩套機制可以同時存在、互不衝突。

**已知的取捨**：這個監聽器沒辦法區分「這是 tpa 接受/回重生點的傳送」還是「單純被人用 `/tp` 傳送去別的地方」——
只要 cause 是 COMMAND 就會解除隱身。如果你的伺服器上，管理員常常會用 `/tp` 之類的指令把隱身中的玩家
傳送去別處（跟 tpa/spawn 無關），那次傳送也會連帶把隱身解除掉。如果這對你造成困擾，跟我說，我可以
改成白名單模式（只認特定幾個指令名稱）。

## 權限節點
| 權限 | 說明 | 預設 |
|---|---|---|
| `vanishtp.use` | 可使用 `/fkcvanish` `/fkcvtp` `/fkctpa` 等指令 | false（需手動給指定玩家） |
| `vanishtp.see` | 即使對方隱身也能看到（給其他巡查員/管理員） | op |
| `vanishtp.forceoff` | `/fkcvanish` 可強制關閉自己的隱身，略過 fkctpa/fkcspawn 限制 | false（預設沒人有，包含 OP） |
| `vanishtp.god` | 可以使用 `/fkcgod` 切換無敵狀態 | false（需手動給指定玩家） |
| `vanishtp.tpcoords` | 可以用 `/fkcvtp` 傳送到指定座標（不影響傳送到玩家） | false（需手動給指定玩家） |
| `vanishtp.locate` | 可以用 `/fkclocate` 搜尋指定地形的座標 | false（需手動給指定玩家） |
| `vanishtp.breakbarrier` | 手持屏障方塊時可以直接破壞屏障方塊 | false（需手動給指定玩家） |

用 LuckPerms 舉例，給某玩家隱身巡查權限：
```
/lp user <玩家名> permission set vanishtp.use true
```

## config.yml 重點
```yaml
bypass-permissions:
  - "worldguard.region.bypass.*"
```
這裡填的是「隱身時暫時附加的權限節點」。**這部分一定要依你實際用的保護區/領地插件調整**：
- 如果你用 **WorldGuard**：`worldguard.region.bypass.*`（或 `worldguard.region.bypass.<世界名>`）
  本來就是官方的「無視所有 region 旗標」權限，包含 move / teleport 相關的旗標，符合你的需求。
- 如果你用的是其他保護區插件（例如自製的、或非 WorldGuard 系統），要去查它是否也提供「permission 形式」的
  bypass 節點；如果它是用白名單/黑名單或事件優先權硬擋、完全不看權限，這個做法就不會生效，需要另外對接
  它的 API（把它的 bypass 方法包一層，在 `VanishManager#vanish/unvanish` 裡呼叫）。

## Folia 相容性重點
- 完全沒有使用 `Bukkit.getScheduler()`（在 Folia 是被停用/no-op 的）。
- 玩家間傳送一律用 `Entity#teleportAsync(Location)`，這是 Paper 為 Folia 設計的跨 region 安全傳送 API，
  回傳 `CompletableFuture<Boolean>`，在 `thenAccept` 裡處理完成後續動作（發訊息、解除隱身）。
- 操作「別人」的 Player 物件（顯示/隱藏）一律透過 `player.getScheduler().run(...)`（Entity Scheduler）
  排程到該玩家自己所屬的 region 執行緒上執行，避免跨執行緒直接存取。
- 延遲任務（tpa 逾時）使用 `Bukkit.getAsyncScheduler().runDelayed(...)`，不影響任何 region 的 tick。

## 建置
```
mvn clean package
```
產生的 `target/fkcvtp-1.0.0.jar` 放到 `plugins/` 資料夾即可（版本號跟著 `pom.xml` 的 `<version>` 走，
之後改版本記得同步改這裡跟 `.github/workflows/build.yml` 裡的路徑）。

`pom.xml` 裡 `paper-api` 的版本號 (`1.21.8-R0.1-SNAPSHOT`) 請對照你伺服器實際使用的 build 版本調整，
若編譯抓不到該版本，去 https://repo.papermc.io 確認目前可用的版號。

## 已知需要你確認/調整的地方
1. **`bypass-permissions` 一定要對照你實際的保護區插件填寫**，否則「隱身時無視保護區限制」不會真的生效。
2. 目前隱身用的是 Bukkit 內建的 `hidePlayer`/`showPlayer`，只處理「玩家可見度」，沒有處理聊天訊息前綴、
   Tab 清單、記分板等額外細節，如果你需要這些，可以再加。
3. `/fkclocate` 內部借用了原版 `/locate biome` 指令，如果實測發現地形名稱打對了卻一直「搜尋失敗」，
   把 console 當下的完整錯誤訊息貼出來，再依實際錯誤調整語法。
