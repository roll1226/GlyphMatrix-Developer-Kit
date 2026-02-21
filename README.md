# Glyph Matrix Developer Kit


Glyph Matrix Developer Kitは、対応デバイス上で**アプリ内に**カスタムGlyph Matrix体験を作成したり、**独自のGlyph Toyを構築**したりする前に知っておくべきすべてを提供します。

その中核となるのは、デザインをGlyph Matrixデータに変換し、Glyph Matrix上でフレームごとにレンダリングできるGlyph Matrix Androidライブラリです。また、Glyph Buttonに関連するイベントを処理できるように識別子も提供します。


このドキュメントには以下の3つのセクションが含まれています

- [**はじめに**](#はじめに): Glyph Matrix Androidライブラリの統合方法、開発環境の設定、Glyph Toysのプレビュー画像の作成方法
- [**Glyph Toyサービスの開発**](#glyph-toyサービスの開発): Glyph Toyのサービスライフサイクルの管理方法、インタラクションの処理方法、AOD機能を持つ場合のトイサービスの動作
- [**APIリファレンス**](#apiリファレンス): クラス、メソッドの完全なドキュメント

このREADMEのサンプルコードはJavaで書かれていますが、Glyph Toys構築を学ぶための完全なデモプロジェクトも提供しており、[GlyphMatrix-Example-Project](https://github.com/KenFeng04/GlyphMatrix-Example-Project)を参照できます。

## はじめに


### 1. Glyph Matrixライブラリの統合

1. 新しいAndroidプロジェクトを作成した後、メインアプリモジュール配下にlibsフォルダを作成します。
2. このリポジトリからAndroidライブラリ（例：GlyphMatrixSDK.aarファイル）をlibsディレクトリにコピーします。
3. build.gradleファイルにライブラリ依存関係としてライブラリを追加します。Android Studioを使用している場合は、[developer.android.com](https://developer.android.com/studio/projects/android-library#psd-add-aar-jar-dependency)の「Add your AAR or JAR as a dependency」セクションを参照して方法を学ぶことができます。
使用するパスが「libs/GlyphMatrixSDK.aar」のようになっていることを確認してください。

### 2. AndroidManifest.xmlの設定

AndroidManifest.xmlファイルは次のパスにあります：`<your-project>/app/src/main/AndroidManifest.xml`

#### 2.1. 必要なパーミッション

AndroidManifest.xmlの`<manifest>`タグ内に以下の行を追加します。

```xml
<uses-permission android:name="com.nothing.ketchum.permission.ENABLE"/>
```

#### 2.2 Glyph Toysのサービス登録

> **注意**: このセクションはGlyph Toysを開発している場合にのみ必要です。既存のアプリケーションにGlyph Matrixライブラリを統合するだけの場合は、このセクションをスキップできます。

Nothing Phoneの設定がGlyph Toysを認識して表示できるようにするには、`AndroidManifest.xml`ファイルの`<application>`タグ内に各トイをサービスとして登録する必要があります。
以下のコードは、2つのトイを登録する方法を示しています。各サービスには、トイの名前、プレビュー画像、サポートされる動作（オプション）のメタデータが含まれています。

プレビュー画像を準備するには、[セクション3: Glyph Toyプレビューの作成](#3-glyph-toyプレビューの作成)を参照してください。

<img src="image/Glyph Toy AndroidManifest.xml.svg" alt="100widget @Glyph Toy AndroidManifest.xml" width="900"/>


最初の例はオプション機能を含む完全なセットアップを提供し、2番目の例は最小限の構成を示しています。

**注意**: 使用する前に、以下の例のクラス名とリソース参照を独自のものに置き換えてください。<br>

```xml
<!-- 完全な例: 拡張機能のための完全なメタデータを持つGlyph Toyを登録 -->
<!-- "com.nothing.demo.TestToyOne"をあなたのサービスクラスに置き換えてください -->
<service android:name="com.nothing.demo.TestToyOne"
    android:exported="true">
    <intent-filter>
        <action android:name="com.nothing.glyph.TOY"/>
    </intent-filter>

    <!-- 必須: トイがGlyph Toysマネージャーリストに表示されるようにします -->
    <meta-data
        android:name="com.nothing.glyph.toy.name"
        android:resource="@string/toy_name_one"/>  <!-- あなたの文字列リソースに置き換えてください -->

    <!-- 必須: ユーザーが設定でトイをプレビューできるようにします -->
    <meta-data
        android:name="com.nothing.glyph.toy.image"
        android:resource="@drawable/img_toy_preview_one"/>  <!-- あなたの画像リソースに置き換えてください -->

    <!-- オプション: トイの簡単な説明を提供します -->
    <meta-data
        android:name="com.nothing.glyph.toy.summary"
        android:resource="@string/toy_summary" />  <!-- あなたの文字列リソースに置き換えてください -->

    <!-- オプション: トイの詳細な紹介ページにリンクします -->
    <meta-data
        android:name="com.nothing.glyph.toy.introduction"
        android:value="com.yourPackage.yourToyIntroduceActivity" />  <!-- あなたのアクティビティクラスに置き換えてください -->

    <!-- オプション: 長押し機能を有効にします、デフォルトは0 -->
    <meta-data
        android:name="com.nothing.glyph.toy.longpress"
        android:value="1"/>

    <!-- オプション: Always-On Display (AOD) のサポートを示します -->
    <meta-data
        android:name="com.nothing.glyph.toy.aod_support"
        android:value="1"/>
</service>

<!-- 最小限の例: 必須のメタデータを持つ基本的なGlyph Toyを登録 -->
<service android:name="com.nothing.demo.TestToySecond"
    android:exported="true">
    <intent-filter>
        <action android:name="com.nothing.glyph.TOY"/>
    </intent-filter>

    <!-- 必須: トイがGlyph Toysマネージャーリストに表示されるようにします -->
    <meta-data
        android:name="com.nothing.glyph.toy.name"
        android:resource="@string/toy_name_second"/>

    <!-- 必須: ユーザーが設定でトイをプレビューできるようにします -->
    <meta-data
        android:name="com.nothing.glyph.toy.image"
        android:resource="@drawable/img_toy_preview_second"/>
</service>
```

#### 3 Glyph Toyプレビューの作成

公式トイと一致し、ユーザーに一貫した体験を提供するGlyph Toyプレビュー画像を作成するには、以下の仕様と[Figmaテンプレート](https://www.figma.com/design/ryjvvPM2ZxI3OGdajSzb5J/Glyph-Toy--preview-icon-template?node-id=1-12&t=HvVOxxNmb5EK2i2g-1)を参照できます。また、任意の1:1デザイン画像をGlyph Matrixプレビュー画像に自動変換して時間を節約できる[Figmaプラグイン](https://www.figma.com/community/plugin/1526505846480298025)も作成しました :)

プレビュー画像をSVGとしてエクスポートすることをお勧めします。SVGプレビューをプロジェクトにインポートする方法については、Android studioドキュメントの[Running Vector Asset Studio](https://developer.android.com/studio/write/vector-asset-studio#svg)セクションを確認してください。


<p align="center">
  <img src="image/Phone 3 Glyph Toy icon specification.svg" width="100%" alt="Phone 3 Glyph Toy icon specification">
</p>





## Glyph Toyサービスの開発

### Glyph Buttonとのユーザーインタラクション

`Glyph Toys`は通常、デバイス背面の`Glyph Button`を使用して制御されます。`Glyph Button`とのインタラクションには主に3つのタイプがあります：

- **短押し**: `Glyph Button`を素早く押すと、利用可能なトイを順番に切り替えます。トイが選択されて`Glyph Matrix`に表示されると、その機能が開始されます。もう一度タップすると、次のトイに移動します。
- **長押し**: `Glyph Button`を長押しすると、現在選択されているトイに`"change"`イベントが送信され、定義したアクションがトリガーされます。例えば、プリインストールされているカメラトイでは、最初の長押しでカメラが起動し、その後の押下で写真が撮影されます。タイマートイの場合は、開始/停止を切り替えます。（この機能は`AndroidManifest.xml`で有効にする必要があります）
- **タッチダウン＆タッチアップ**: ユーザーが`Glyph Button`を押し続けて離すと、`"action_down"`と`"action_up"`イベントがトリガーされます

さらに、トイはジャイロスコープや加速度計などの他の制御入力も利用して、コミュニティメンバーと共同制作した`Magic 8 Ball`や`Leveler Toy`のような、より魅力的な体験を作成できます。一度にアクティブにできるトイは1つだけであることに注意してください。

### 開発実装

#### トイ選択への応答（ライフサイクルの管理）

ユーザーがトイを選択すると、システムはサービスをバインドします。機能を開始するためにonBind()を実装し、クリーンに停止するためにonUnbind()を実装する必要があります。

以下の例は、開始/停止ライフサイクルを管理する方法を示しています。

```java
@Override
public IBinder onBind(Intent intent) {
    init();// Glyph Toyが選択されたときに作成したい体験
    return null;
}

@Override
public boolean onUnbind(Intent intent) {
    mGM.unInit();
    mGM = null;
    mCallback = null;
    return false;
}
```

#### "Change"イベントの処理

Glyph Button関連のイベントはGlyphToyクラスを使用して処理されます。Glyph Buttonへのユーザーのインタラクションに反応するには、イベントを処理する`Handler`とシステムと通信する`Messenger`を作成する必要があります。この`Messenger`からの`IBinder`は、`onBind()`メソッドによって返される必要があります。

以下のコードは、イベントを受信して処理する方法を示しています。この場合は長押し（change）イベントです。

```java
@Override
public IBinder onBind(Intent intent) {
    init();
    return serviceMessenger.getBinder();
}

private final Handler serviceHandler = new Handler(Looper.getMainLooper()) {
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case GlyphToy.MSG_GLYPH_TOY: {
                Bundle bundle = msg.getData();
                String event = bundle.getString(GlyphToy.MSG_GLYPH_TOY_DATA);
                if (GlyphToy.EVENT_CHANGE.equals(event)) {
                    // 長押しに対するあなたの反応
                }
                break;
            }
            default:
                super.handleMessage(msg);
        }
    }
};
private final Messenger serviceMessenger = new Messenger(serviceHandler);
```

### AOD機能を持つトイ
トイをAODトイとして設定した場合、トイがAODトイとして選択されているときに、毎分EVENT_AODを受信します。



```java
private final Handler serviceHandler = new Handler(Looper.getMainLooper()) {
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case GlyphToy.MSG_GLYPH_TOY: {
                Bundle bundle = msg.getData();
                String event = bundle.getString(GlyphToy.MSG_GLYPH_TOY_DATA);
                if (GlyphToy.EVENT_AOD.equals(event)) {
                    // AODに対するあなたのアクション
                }
                break;
            }
            default:
                super.handleMessage(msg);
        }
    }
};
```

### ベストプラクティス: ユーザーをGlyph Toysマネージャーに誘導

Glyph Toyアプリケーションの配布準備が整ったら、以下のインテントアクションを使用することを強くお勧めします。これはシステムバージョン20250829以降で有効です。

このインテントはユーザーをシステムのGlyph Toysを管理画面に誘導し、アクティブなトイキューにトイを追加できるようにします。このガイダンスがないと、ユーザーはアプリケーションをインストールした後、トイをアクティブにする方法がわからず混乱する可能性があります。
```java
Intent intent = new Intent();
intent.setComponent(new ComponentName("com.nothing.thirdparty", "com.nothing.thirdparty.matrix.toys.manager.ToysManagerActivity"));
startActivity(intent);
```

以下は2つの一般的な実装シナリオです：

1. シンプルなトイの場合：アプリケーションインターフェースには、トイの基本的な紹介と「トイをアクティブ化」などのボタンを配置し、インテントをトリガーできます。

2. セットアップが必要なトイの場合：設定プロセスの最後にこのインテントをトリガーするボタンを配置することをお勧めします。これにより、ユーザーはセットアップを完了した直後にトイをアクティブにするように誘導されます。例えば、YouTubeチャンネルの登録者数を表示するように設計されたトイの場合、機能するためにはセットアップの一環としてまずユーザーがチャンネルを選択する必要があります。

## APIリファレンス

### GlyphMatrixManager

GlyphMatrixManagerは以下を担当します：

- 基盤となるサービスまたはデバイスへの**接続と切断**
- そのサービス/デバイスを使用するための**アプリケーションの登録**
- 生のカラーデータまたは構造化されたフレームオブジェクトのいずれかを使用したGlyph Matrixの**ディスプレイの更新**

Glyph ToyサービスからGlyph Matrixにコンテンツを表示することに加えて、GDKを使用して独自のアプリから直接Glyph Matrixを制御できます。アプリベースの制御には、`setMatrixFrame`の代わりに常に`setAppMatrixFrame`関数を使用してください。後者はアクティブなGlyph Toysと競合する可能性があります。この機能には、20250801以降の電話システムバージョンが必要です。

注意：Glyph Toyは、Glyph Matrixでのサードパーティアプリの使用よりも高い表示優先度を持ちます。ユーザーがGlyph Buttonと対話すると、トリガーされるGlyph ToyカルーセルがアプリのコンテンツをMatrixから上書きします。

<p align="center">
  <img src="image/Glyph Matrix Display Priority.svg" alt="Glyph Matrix Priority" style="display:block; width:100%; max-width:100%;">
</p>



#### パブリックメソッド

| 戻り値の型                    | メソッド                         | 説明                    |
|:-------------------------------|:-------------------------------|:-------------------------------|
| void           | `init(Callback callback)`       | サービスのバインドに使用します。コンポーネントの開始時に作成することをお勧めします。            |
| void           | `unInit()`                      | サービスのアンバインドに使用します。コンポーネントの終了時に破棄することをお勧めします。          |
| void           | `closeAppMatrix()`              | アプリマトリクス表示を閉じます。この関数を使用して、アプリからGlyph Matrixへのコンテンツ表示を停止します。20250801以降の電話システムバージョンが必要です。 |
| void           | `register(String target)`       | サービスにアプリを登録します。作業対象のターゲットデバイスを送信する必要があります。Phone 3の場合、これはGlyph.DEVICE_23112である必要があります。|
| void           | `setMatrixFrame(int[] color)`   | 生のカラーデータを使用してGlyph Matrixディスプレイを更新します。このオーバーロードは25x25の整数配列を期待します。 |
| void           | `setMatrixFrame(GlyphMatrixFrame)` | 構造化されたGlyphMatrixFrameオブジェクトを使用してGlyph Matrixディスプレイを更新します。                 |
| void           | `setAppMatrixFrame(int[] color)` | setMatrixFrame(int[] color)と同じです。アプリでGlyph Matrixを使用したい場合は、この関数を使用してGlyph Matrixディスプレイを更新してください。20250801以降の電話システムバージョンが必要です。 |
| void           | `setAppMatrixFrame(GlyphMatrixFrame frame)` | setMatrixFrame(GlyphMatrixFrame frame)と同じです。アプリでGlyph Matrixを使用したい場合は、この関数を使用してGlyph Matrixディスプレイを更新してください。20250801以降の電話システムバージョンが必要です。 |


### GlyphMatrixFrame

GlyphMatrixFrameは、Glyph Matrixの処理と表示を担当します。デフォルトのサイズは25x25ですが、Builderを使用して簡単にカスタマイズできます。 

<p align="center">
  <div align="center" style="width:100%;">
    <img src="image/Phone 3 Glyph Matrix LED allocation.svg" alt="Phone 3 Glyph Matrix LED allocation" style="display:block; width:100%; max-width:100%;">
  </div>
</p>

開発者はこのクラスを使用して、GlyphMatrixオブジェクトを必要なMatrixデータにレンダリングし、ビルダーを使用してレイヤーに基づいてGlyph Matrix上に複数のオブジェクトをオーバーレイする方法を制御できます。

#### パブリックメソッド

| 戻り値の型                    | メソッド名                    | 説明                    |
|:-------------------------------|:-------------------------------|:-------------------------------|
| int[]                          |  `render()`              | **Builder**経由で以前に追加されたすべてのオブジェクトをレンダリングし、対応する**Glyph Matrixデータ**に統合します。このメソッドの実装はプロジェクトの要件によって異なる場合があります。このメソッドは、実際のGlyph Matrix上に表示するためにハードウェアを直接駆動するか、さらなる処理のために配列またはビットマップデータを呼び出し元に返すことができます。 |

### GlyphMatrixFrame.Builder

GlyphMatrixFrame.Builderは、GlyphMatrixFrameを構築および設定するのに役立つBuilderパターンを使用する静的内部クラスです。このフレームは本質的に、マトリクス上の各LEDがどのように点灯するかを定義するデータの配列であり、GlyphMatrixManagerによって読み取ることができます。

最終的に`build()`を呼び出して完成したGlyphMatrixFrameインスタンスを生成する前に、各GlyphMatrixFrameに対して最大3つのGlyphMatrixObject（各レイヤーに1つ）を持つことができます。

#### パブリックコンストラクタ

| 戻り値の型                    | メソッド名                    | 説明                    |
|:-------------------------------|:-------------------------------|:-------------------------------|
| N/A            | `Builder()`                     | 新しい`Builder`インスタンスを初期化します。これにより、設定準備が整った空の`GlyphMatrixFrame.Builder`が作成されます。デフォルトでは、Matrixサイズは25x25に設定されています。 |

#### パブリックメソッド

| 戻り値の型                    | メソッド                         | 説明                    |
|:-------------------------------|:-------------------------------|:-------------------------------|
| void           | `addTop(GlyphMatrixObject object)` | オブジェクトを最上位レイヤーに追加し、中間および下位レイヤーの上にレンダリングされます |
| void           | `addMid(GlyphMatrixObject object)` | オブジェクトを中間レイヤーに追加し、最上位と下位レイヤーの間にレンダリングされます |
| void           | `addLow(GlyphMatrixObject object)` | オブジェクトを最下位レイヤーに追加し、最上位および中間レイヤーの下にレンダリングされます |
| GlyphMatrixFrame | `build(Context context)`        | 現在蓄積された設定に基づいて新しいGlyphMatrixFrameインスタンスを構築して返します。このインスタンスは表示またはさらなる操作の準備ができています。 |
#### 例
以下の例は、最上位レイヤーにバタフライオブジェクトを持つGlyphMatrixFrameを作成し、実際のマトリクスを管理する前述のクラスmGM（GlyphMatrixManager）を使用して実際のGlyph Matrixにレンダリングする方法を示しています。
```java
GlyphMatrixFrame.Builder frameBuilder = new GlyphMatrixFrame.Builder();
GlyphMatrixFrame frame = frameBuilder.addTop(butterfly).build();
mGM.setMatrixFrame(frame.render());
```

### GlyphMatrixObject

GlyphMatrixObjectは、Glyph Matrix上に表示する単一の画像またはフレームを、その設定可能なプロパティとともにカプセル化します。

画像ソース、位置座標（X、Y）、時計回りの回転角度、スケーリング、輝度、透明度などのプロパティは、すべて`GlyphMatrixObject.Builder`を使用して調整できます。

#### パブリックアクセサメソッド

| 戻り値の型                    | メソッド名                    | 説明                    |
|:-------------------------------|:-------------------------------|:-------------------------------|
| Object         | `getImageSource()`            | 画像ソースオブジェクトを返します|
| Int            | `getPositionX()`              | オブジェクトの左上隅のX座標を返します。                                       |
| Int            | `getPositionY()`              | オブジェクトの左上隅のY座標を返します。                                       |
| Int            | `getOrientation()`            | オリジナルからのオブジェクトの反時計回りの回転角度を返します（デフォルト：0）       |
| Int            | `getScale()`                  | オブジェクトの**スケーリング係数**を返します。（0-200、デフォルト：100）                           |
| Int            | `getBrightness()`             | オブジェクトの輝度**レベル**を返します。（0-255、デフォルト：255）                         |

### GlyphMatrixObject.Builder

GlyphMatrixObject.Builderは、GlyphMatrixObjectの静的内部クラスで、画像オブジェクトの表示パラメータを作成および設定するために使用されます。

#### パブリックコンストラクタ

| 戻り値の型                    | メソッド名                    | 説明                    |
|:-------------------------------|:-------------------------------|:-------------------------------|
| N/A            | `Builder()`                     | 他のメソッドによって明示的に設定されていない場合、`Builder`は`GlyphMatrixObject`に対して以下のデフォルト値を使用します：<br/>- 位置：(0, 0)<br/>- 方向：0度<br/>- スケール：100<br/>- 輝度：255 |

#### パブリックメソッド

| 戻り値の型                    | メソッド名                    | 説明                    |
|:-------------------------------|:-------------------------------|:-------------------------------|
| void           | `setImageSource(Object imagesource)` | 画像ソースを設定します。1:1のビットマップである必要があります - 他の形式は変換が必要です。高解像度はパフォーマンスに影響を与える可能性があります。 |
| void           | `setText(String text)`          | Glyph Matrixに表示される文字列コンテンツを設定します。                                   |
| void           | `setPosition(int x, int y)`     | Glyph Matrix上のオブジェクトの左上隅の位置を設定します                                 |
| void           | `setOrientation(int)`           | 中心を基準としたオブジェクトの時計回りの回転角度を度数で設定します。0 = 回転なし（デフォルト）。値は0-360°の範囲に正規化されます |
| void           | `setBrightness(int brightness)` | オブジェクトの輝度レベルを設定します。<br/><br/>許容値は0（LED消灯）から255（最大輝度、デフォルト）までの範囲です。255を超える値は自動的に255に制限されます。 |
| void           | `setScale(int scale)`           | オブジェクトのスケーリング係数を設定します。アンカーポイントはオブジェクトの中央にあります<br/><br/>有効範囲：0-200 0：オブジェクトは表示されません 100：オリジナルサイズ（デフォルト） 200：2倍サイズ |
| GlyphMatrixObject | `build()`                    | 現在の設定に基づいてGlyphMatrix Objectインスタンスを構築して返します。             |

### 例
以下の例は、butterflyと呼ばれるGlyphMatrixObjectを構築する方法を示しています。

```java
GlyphMatrixObject.Builder butterflyBuilder = new GlyphMatrixObject.Builder();
GlyphMatrixObject butterfly = butterflyBuilder
.setImageSource(GlyphMatrixUtils.drawableToBitmap(getResources().getDrawable(R.drawable.butterfly)))
.setScale(100)
.setOrientation(0)
.setPosition(0, 0)
.setReverse(false)
.build();
```

## 関連項目
### Glyph Toyサービスの完全な例

以下のコードブロックは、JavaでのGlyph Toyサービスの完全な実装を示しています。GlyphMatrixManagerを初期化し、デバイスを登録し、カスタムGlyphMatrixObject（例えばバタフライ画像）をGlyph Matrix上に表示する方法を示します。
```java
@Override
public IBinder onBind(Intent intent) {
    init();
    return null;
}

@Override
public boolean onUnbind(Intent intent) {
    mGM.turnOff();
    mGM.unInit();
    mGM = null;
    mCallback = null;
    return false;
}

private void init() {
    mGM = GlyphMatrixManager.getInstance(getApplicationContext());
    mCallback = new GlyphMatrixManager.Callback() {
        @Override
        public void onServiceConnected(ComponentName componentName) {
            mGM.register(Glyph.DEVICE_23112);
            action();
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };
    mGM.init(mCallback);
}

private void action() {
    GlyphMatrixObject.Builder butterflyBuilder = new GlyphMatrixObject.Builder();
    GlyphMatrixObject butterfly = butterflyBuilder
            .setImageSource(GlyphMatrixUtils.drawableToBitmap(getResources().getDrawable(R.drawable.butterfly)))
            .setScale(100)
            .setOrientation(0)
            .setPosition(0, 0)
            .setReverse(false)
            .build();
    
    GlyphMatrixFrame.Builder frameBuilder = new GlyphMatrixFrame.Builder();
    GlyphMatrixFrame frame = frameBuilder.addTop(butterfly).build();
    mGM.setMatrixFrame(frame.render());
}
```


### その他の有用なリソース

Glyph Toysを構築するための実践的なデモプロジェクトについては、[GlyphMatrix-Example-Project](https://github.com/KenFeng04/GlyphMatrix-Example-Project)をご覧ください<br>
Glyph Light Stripeを搭載したデバイス向けのGlyph Interface体験を構築するためのキット [Glyph-Developer-Kit](https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit)

## サポート

このキットにエラーを見つけた場合は、issueを登録してください。

開発に関する問題がある場合は、[GDKsupport@nothing.tech](mailto:GDKsupport@nothing.tech)にお問い合わせください。

ただし、[コミュニティ](https://nothing.community/t/glyph-sdk)からより早い応答を得られる場合があります
