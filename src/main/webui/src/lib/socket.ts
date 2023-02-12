import { writable, type Writable } from "svelte/store";

function configStateStore() {
  let sock;
  const { update, subscribe } = writable<any[]>([], () => {
    // on setup
    // runs after first subscriber
    sock = new WebSocket((window.location.origin + '/web').replace('http', 'ws'));

    sock.addEventListener('open', (event) => {
      sock.send('Hello Server!');
    });
    sock.addEventListener('message', (event) => {
      console.log('Message from server ', event.data);
      update((state) => state.concat(event.data));
    });
    sock.readyState

    return () => {
      // on teardown
      // runs after last subscriber unsubscribes
      sock.close();
    };
  });

  function send(message: any) {
    console.log('Send to the server ', JSON.stringify(message));
    sock.send(JSON.stringify(message));
  }

  return {
    subscribe,
    send,
  };
}




