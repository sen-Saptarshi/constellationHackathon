const { dag4 } = require("@stardust-collective/dag4");
const jsSha256 = require("js-sha256");
const axios = require("axios");

const buildCreatePost = () => {
  return {
    CreatePost: { content: "This is a My post," },
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
    Subscribe: { userId: "DAG5sRBaVoDfDfwavAQoQ1fm582MJiCV1sZk6QXm" },
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
  //   const walletPrivateKey =
  //     "95ccea32029c497cce1e4755e7acd0db8aed8ce681b22d085505cf08f812037f";

  const walletPrivateKey =
    "7a6dcea6523ce31337fe405ef1cb53b4b15dc8f7f59c3cd4978efc30eac5c4fc";

  const account = dag4.createAccount();
  account.loginPrivateKey(walletPrivateKey);
  console.log("User_ID: " + account.address);

  account.connect({
    networkVersion: "2.0",
    l0Url: globalL0Url,
    testnet: true,
  });

  const message = buildSubscribe();
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


/* 

<metagraph l0 url>/data-application/users/:user_id/posts: Returns all user posts.
<metagraph l0 url>/data-application/users/:user_id/subscriptions: Returns the subscriptions of a user.
<metagraph l0 url>/data-application/users/:user_id/feed

*/