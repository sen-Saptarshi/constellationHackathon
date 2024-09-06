const { dag4 } = require("@stardust-collective/dag4");
const jsSha256 = require("js-sha256");
const axios = require("axios");

const buildCreatePost = () => {
  return {
    CreatePost: { content: "This is a post" },
  };
};

const buildEditPost = () => {
  return {
    EditPost: { postId: "", content: "" },
  };
};

const buildDeletePost = () => {
  return {
    DeletePost: { postId: "" },
  };
};

const buildSubscribe = () => {
  return {
    Subscribe: { userId: "" },
  };
};

/** Encode message according with serializeUpdate on your template module l1 */
const getEncoded = (value) => {
  const energyValue = JSON.stringify(value);
  return energyValue;
};

const serialize = (msg) => {
  const coded = Buffer.from(msg, "utf8").toString("hex");
  return coded;
};

const generateProof = async (message, walletPrivateKey, account) => {
  const encoded = getEncoded(message);

  const serializedTx = serialize(encoded);
  const hash = jsSha256.sha256(Buffer.from(serializedTx, "hex"));
  const signature = await dag4.keyStore.sign(walletPrivateKey, hash);

  const publicKey = account.publicKey;
  const uncompressedPublicKey =
    publicKey.length === 128 ? "04" + publicKey : publicKey;

  return {
    id: uncompressedPublicKey.substring(2),
    signature,
  };
};

const sendDataTransactionsUsingUrls = async (
  globalL0Url,
  metagraphL1DataUrl
) => {
  const walletPrivateKey =
    "95ccea32029c497cce1e4755e7acd0db8aed8ce681b22d085505cf08f812037f";

  const account = dag4.createAccount();
  account.loginPrivateKey(walletPrivateKey);

  account.connect({
    networkVersion: "2.0",
    l0Url: globalL0Url,
    testnet: true,
  });

  const message = buildCreatePost();
  const proof = await generateProof(message, walletPrivateKey, account);
  const body = {
    value: {
      ...message,
    },
    proofs: [proof],
  };
  try {
    console.log(`Transaction body: ${JSON.stringify(body)}`);
    const response = await axios.post(`${metagraphL1DataUrl}/data`, body);
    console.log(`Response: ${JSON.stringify(response.data)}`);
  } catch (e) {
    console.log("Error sending transaction", e.message);
  }
  return;
};

const sendDataTransaction = async () => {
  const globalL0Url = "http://localhost:9000";
  const metagraphL1DataUrl = "http://localhost:9400";

  await sendDataTransactionsUsingUrls(globalL0Url, metagraphL1DataUrl);
};

sendDataTransaction();
